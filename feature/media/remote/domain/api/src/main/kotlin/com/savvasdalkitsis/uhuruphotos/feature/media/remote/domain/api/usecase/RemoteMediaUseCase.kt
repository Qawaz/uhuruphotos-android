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
package com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.usecase

import com.github.michaelbull.result.Result
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.entities.media.DbRemoteMediaItemDetails
import com.savvasdalkitsis.uhuruphotos.feature.db.domain.api.entities.media.DbRemoteMediaItemSummary
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.service.model.RemoteMediaCollection
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.service.model.RemoteMediaCollectionsByDate
import com.savvasdalkitsis.uhuruphotos.foundation.result.api.SimpleResult
import kotlinx.coroutines.flow.Flow

interface RemoteMediaUseCase {

    fun observeAllRemoteMediaDetails(): Flow<List<DbRemoteMediaItemDetails>>

    fun observeFavouriteRemoteMedia(): Flow<com.github.michaelbull.result.Result<List<DbRemoteMediaItemSummary>, Throwable>>

    suspend fun observeRemoteMediaItemDetails(id: String): Flow<DbRemoteMediaItemDetails>

    suspend fun getRemoteMediaItemDetails(id: String): DbRemoteMediaItemDetails?

    fun observeHiddenRemoteMedia(): Flow<List<DbRemoteMediaItemSummary>>

    suspend fun getFavouriteMediaSummariesCount(): com.github.michaelbull.result.Result<Long, Throwable>

    suspend fun getHiddenMediaSummaries(): List<DbRemoteMediaItemSummary>

    suspend fun setMediaItemFavourite(id: String, favourite: Boolean): SimpleResult

    suspend fun refreshDetailsNowIfMissing(id: String): SimpleResult

    suspend fun refreshDetailsNow(id: String): SimpleResult

    suspend fun refreshFavouriteMedia(): SimpleResult

    suspend fun refreshHiddenMedia(): SimpleResult

    fun trashMediaItem(id: String)

    fun deleteMediaItems(vararg ids: String)

    fun restoreMediaItem(id: String)

    suspend fun processRemoteMediaCollections(
        albumsFetcher: suspend () -> RemoteMediaCollectionsByDate,
        remoteMediaCollectionFetcher: suspend (String) -> RemoteMediaCollection.Complete,
        shallow: Boolean,
        onProgressChange: suspend (current: Int, total: Int) -> Unit = { _, _ -> },
        incompleteAlbumsProcessor: suspend (List<RemoteMediaCollection.Incomplete>) -> Unit = {},
        completeAlbumProcessor: suspend (RemoteMediaCollection.Complete) -> Unit = {},
        clearSummariesBeforeInserting: Boolean = true,
    ): SimpleResult

    suspend fun exists(hash: String): Result<Boolean, Throwable>
}