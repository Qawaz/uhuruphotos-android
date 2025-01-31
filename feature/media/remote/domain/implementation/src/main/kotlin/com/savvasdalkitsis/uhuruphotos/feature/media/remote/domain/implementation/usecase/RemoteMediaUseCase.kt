/*
Copyright 2022 Savvas Dalkitsis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.implementation.usecase

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.getOrThrow
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.Database
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.entities.media.DbRemoteMediaItemDetails
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.entities.media.DbRemoteMediaItemSummary
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.extensions.async
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.media.remote.RemoteMediaItemSummaryQueries
import com.savvasdalkitsis.uhuruphotos.feature.media.common.domain.api.model.MediaItemHash
import com.savvasdalkitsis.uhuruphotos.feature.media.common.domain.api.model.MediaOperationResult
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.model.RemoteMediaItemSummary
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.model.toDbModel
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.service.model.RemoteMediaCollection
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.service.model.RemoteMediaCollectionById
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.service.model.RemoteMediaCollectionsByDate
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.service.model.toDbModel
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.usecase.RemoteMediaUseCase
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.implementation.repository.RemoteMediaRepository
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.implementation.service.RemoteMediaService
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.implementation.worker.RemoteMediaItemWorkScheduler
import com.savvasdalkitsis.uhuruphotos.feature.settings.domain.api.usecase.SettingsUseCase
import com.savvasdalkitsis.uhuruphotos.feature.user.domain.api.usecase.UserUseCase
import com.savvasdalkitsis.uhuruphotos.foundation.log.api.andThenTry
import com.savvasdalkitsis.uhuruphotos.foundation.log.api.runCatchingWithLog
import com.savvasdalkitsis.uhuruphotos.foundation.result.api.SimpleResult
import com.savvasdalkitsis.uhuruphotos.foundation.result.api.mapToResultFlow
import com.savvasdalkitsis.uhuruphotos.foundation.result.api.simple
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import se.ansman.dagger.auto.AutoBind
import javax.inject.Inject

@AutoBind
class RemoteMediaUseCase @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val remoteMediaRepository: RemoteMediaRepository,
    private val remoteMediaService: RemoteMediaService,
    private val remoteMediaItemWorkScheduler: RemoteMediaItemWorkScheduler,
    private val userUseCase: UserUseCase,
    private val remoteMediaItemSummaryQueries: RemoteMediaItemSummaryQueries,
    private val db: Database,
) : RemoteMediaUseCase {

    override fun observeAllRemoteMediaDetails(): Flow<List<DbRemoteMediaItemDetails>> =
        remoteMediaRepository.observeAllMediaItemDetails()

    override fun observeFavouriteRemoteMedia(): Flow<Result<List<DbRemoteMediaItemSummary>, Throwable>> =
        flow {
            emitAll(
                withFavouriteThreshold { threshold ->
                    remoteMediaRepository.observeFavouriteMedia(threshold)
                }.mapToResultFlow()
            )
        }

    override fun observeHiddenRemoteMedia(): Flow<List<DbRemoteMediaItemSummary>> =
        remoteMediaRepository.observeHiddenMedia()

    override fun observeRemoteMediaItemDetails(id: String): Flow<DbRemoteMediaItemDetails> =
        remoteMediaRepository.observeMediaItemDetails(id).distinctUntilChanged()

    override suspend fun getRemoteMediaItemDetails(id: String): DbRemoteMediaItemDetails? =
        remoteMediaRepository.getMediaItemDetails(id)

    override suspend fun getRemoteMediaItemSummary(id: String): DbRemoteMediaItemSummary? =
        remoteMediaRepository.getOrRefreshRemoteMediaItemSummary(id)

    override suspend fun getFavouriteMediaSummariesCount(): Result<Long, Throwable> = withFavouriteThreshold {
        remoteMediaRepository.getFavouriteMediaCount(it)
    }

    override suspend fun getHiddenMediaSummaries(): List<DbRemoteMediaItemSummary> =
        remoteMediaRepository.getHiddenMedia()

    override suspend fun setMediaItemFavourite(id: String, favourite: Boolean): SimpleResult =
        userUseCase.getUserOrRefresh().andThenTry {
            remoteMediaRepository.setMediaItemRating(id, it.favoriteMinRating?.takeIf { favourite } ?: 0)
            remoteMediaItemWorkScheduler.scheduleMediaItemFavourite(id, favourite)
        }.simple()

    override suspend fun refreshDetailsNowIfMissing(id: String): Result<MediaOperationResult, Throwable> =
        remoteMediaRepository.refreshDetailsNowIfMissing(id)

    override suspend fun refreshDetailsNow(id: String): SimpleResult =
        remoteMediaRepository.refreshDetailsNow(id)

    override suspend fun refreshFavouriteMedia(): SimpleResult =
        resultWithFavouriteThreshold { favouriteThreshold ->
            val currentFavourites = remoteMediaRepository.getFavouriteMedia(favouriteThreshold)
                .map { it.id }
                .toSet()
            val newFavourites = mutableListOf<RemoteMediaItemSummary>()
            refreshMediaCollections(
                incompleteMediaCollections = { remoteMediaService.getFavouriteMedia() },
                clearCollectionsBeforeRefreshing = {},
                completeMediaCollection = { id ->
                    remoteMediaService.getFavouriteMediaCollection(id).also { collection ->
                        newFavourites += collection.results.items
                    }
                },
                processSummary = { albumId, summary ->
                    remoteMediaItemSummaryQueries.insert(summary.toDbModel(albumId))
                }
            )
            val newFavouriteIds = newFavourites.map { it.id }.toSet()
            runCatchingWithLog {
                (currentFavourites - newFavouriteIds).forEach {
                    val rating = remoteMediaService.getMediaItem(it).rating
                    async { remoteMediaItemSummaryQueries.setRating(rating, it) }
                }
            }
        }

    override suspend fun refreshHiddenMedia() = refreshMediaCollections(
        incompleteMediaCollections = { remoteMediaService.getHiddenMedia() },
        clearCollectionsBeforeRefreshing = { remoteMediaItemSummaryQueries.deleteHidden() },
        completeMediaCollection = { remoteMediaService.getHiddenMediaCollection(it) },
        processSummary = { _, summary ->
            remoteMediaItemSummaryQueries.insertHidden(
                id = summary.id,
                dominantColor = summary.dominantColor,
                aspectRatio = summary.aspectRatio,
                location = summary.location,
                rating = summary.rating,
                url = summary.url,
                date = summary.date,
                birthTime = summary.birthTime,
                type = summary.type,
                videoLength = summary.videoLength,
            )
        }
    )

    override fun trashMediaItem(id: String) {
        remoteMediaItemWorkScheduler.scheduleMediaItemTrashing(id)
    }

    override fun deleteMediaItems(vararg ids: String) {
        for (id in ids) {
            remoteMediaItemWorkScheduler.scheduleMediaItemDeletion(id)
        }
    }

    override fun restoreMediaItem(id: String) {
        remoteMediaItemWorkScheduler.scheduleMediaItemRestoration(id)
    }

    override suspend fun processRemoteMediaCollections(
        incompleteAlbumsFetcher: suspend () -> List<RemoteMediaCollection.Incomplete>,
        completeAlbumsFetcher: suspend (String) -> RemoteMediaCollection.Complete,
        shallow: Boolean,
        onProgressChange: suspend (current: Int, total: Int) -> Unit,
        incompleteAlbumsProcessor: suspend (List<RemoteMediaCollection.Incomplete>) -> Unit,
        completeAlbumProcessor: suspend (RemoteMediaCollection.Complete) -> Unit,
        clearSummariesBeforeInserting: Boolean,
    ): SimpleResult = runCatchingWithLog {
        onProgressChange(0, 0)
        val albums = incompleteAlbumsFetcher()
        incompleteAlbumsProcessor(albums)
        val albumsToDownloadSummaries = when {
            shallow -> albums.take(settingsUseCase.getFeedDaysToRefresh())
            else -> albums
        }
        for ((index, incompleteAlbum) in albumsToDownloadSummaries.withIndex()) {
            val id = incompleteAlbum.id
            updateSummaries(id, completeAlbumsFetcher, completeAlbumProcessor, clearSummariesBeforeInserting)
            onProgressChange(index, albumsToDownloadSummaries.size)
        }
    }.simple()

    override suspend fun exists(hash: MediaItemHash): Result<Boolean, Throwable> = runCatchingWithLog {
        remoteMediaService.exists(hash.hash).exists
    }

    override suspend fun refreshMediaCollections(
        incompleteMediaCollections: suspend () -> RemoteMediaCollectionsByDate,
        clearCollectionsBeforeRefreshing: () -> Unit,
        completeMediaCollection: suspend (String) -> RemoteMediaCollectionById,
        processSummary: (albumId: String, summary: RemoteMediaItemSummary) -> Unit,
    ): SimpleResult = runCatchingWithLog {
        val incompleteCollections = incompleteMediaCollections().results
        async {
            db.transaction {
                for (incompleteCollection in incompleteCollections) {
                    remoteMediaRepository.insertMediaCollection(incompleteCollection.toDbModel())
                }
            }
        }
        async { clearCollectionsBeforeRefreshing() }
        for (incompleteCollection in incompleteCollections) {
            val id = incompleteCollection.id
            val completeCollection = completeMediaCollection(id).results
            async {
                completeCollection.items.forEach {
                    processSummary(id, it)
                }
            }
        }
    }.simple()

    private suspend fun updateSummaries(
        id: String,
        remoteMediaCollectionFetcher: suspend (String) -> RemoteMediaCollection.Complete,
        completeAlbumProcessor: suspend (RemoteMediaCollection.Complete) -> Unit,
        clearSummariesBeforeInserting: Boolean = true,
    ) {
        val completeAlbum = remoteMediaCollectionFetcher(id)
        completeAlbumProcessor(completeAlbum)
        async {
            remoteMediaItemSummaryQueries.transaction {
                if (clearSummariesBeforeInserting) {
                    remoteMediaItemSummaryQueries.deletePhotoSummariesforAlbum(id)
                }
                completeAlbum.items
                    .map { it.toDbModel(id) }
                    .forEach {
                        remoteMediaItemSummaryQueries.insert(it)
                    }
            }
        }
    }

    private suspend fun <T> withFavouriteThreshold(action: suspend (Int) -> T): Result<T, Throwable> =
        userUseCase.getUserOrRefresh().andThenTry {
            action(it.favoriteMinRating!!)
        }

    private suspend fun resultWithFavouriteThreshold(action: suspend (Int) -> SimpleResult): SimpleResult =
        userUseCase.getUserOrRefresh().andThenTry {
            action(it.favoriteMinRating!!).getOrThrow()
        }
}