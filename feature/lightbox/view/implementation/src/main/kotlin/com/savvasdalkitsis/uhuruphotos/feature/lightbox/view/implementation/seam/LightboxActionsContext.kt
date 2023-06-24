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
package com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam

import android.content.Context
import android.net.Uri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.savvasdalkitsis.uhuruphotos.feature.album.auto.domain.api.usecase.AutoAlbumUseCase
import com.savvasdalkitsis.uhuruphotos.feature.album.user.domain.api.usecase.UserAlbumUseCase
import com.savvasdalkitsis.uhuruphotos.feature.feed.domain.api.usecase.FeedUseCase
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxDeletionCategory.FULLY_SYNCED_ITEM
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxDeletionCategory.LOCAL_ONLY_ITEM
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxDeletionCategory.REMOTE_ITEM
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxDeletionCategory.REMOTE_ITEM_TRASHED
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxMutation.FinishedLoading
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxMutation.FinishedLoadingDetails
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxMutation.HideAllConfirmationDialogs
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxMutation.Loading
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxMutation.LoadingDetails
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxMutation.RemoveMediaItemFromSource
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.LightboxMutation.ShowErrorMessage
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.effects.CropPhoto
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.effects.LightboxEffect
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.effects.NavigateBack
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.ui.state.LightboxState
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.ui.state.MediaItemType
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.ui.state.MediaItemType.TRASHED
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.ui.state.SingleMediaItemState
import com.savvasdalkitsis.uhuruphotos.feature.local.domain.api.usecase.LocalAlbumUseCase
import com.savvasdalkitsis.uhuruphotos.feature.media.common.domain.api.model.MediaId
import com.savvasdalkitsis.uhuruphotos.feature.media.common.domain.api.usecase.MediaUseCase
import com.savvasdalkitsis.uhuruphotos.feature.media.common.domain.api.usecase.MetadataUseCase
import com.savvasdalkitsis.uhuruphotos.feature.media.local.domain.api.model.LocalMediaDeletionRequest
import com.savvasdalkitsis.uhuruphotos.feature.media.local.domain.api.model.LocalMediaItemDeletion.Error
import com.savvasdalkitsis.uhuruphotos.feature.media.local.domain.api.model.LocalMediaItemDeletion.RequiresPermissions
import com.savvasdalkitsis.uhuruphotos.feature.media.local.domain.api.model.LocalMediaItemDeletion.Success
import com.savvasdalkitsis.uhuruphotos.feature.media.local.domain.api.model.MissingPermissionsException
import com.savvasdalkitsis.uhuruphotos.feature.media.local.domain.api.usecase.LocalMediaDeletionUseCase
import com.savvasdalkitsis.uhuruphotos.feature.media.remote.domain.api.usecase.RemoteMediaUseCase
import com.savvasdalkitsis.uhuruphotos.feature.memories.domain.api.usecase.MemoriesUseCase
import com.savvasdalkitsis.uhuruphotos.feature.person.domain.api.usecase.PersonUseCase
import com.savvasdalkitsis.uhuruphotos.feature.search.domain.api.usecase.SearchUseCase
import com.savvasdalkitsis.uhuruphotos.feature.trash.domain.api.usecase.TrashUseCase
import com.savvasdalkitsis.uhuruphotos.foundation.date.api.module.DateModule
import com.savvasdalkitsis.uhuruphotos.foundation.log.api.log
import com.savvasdalkitsis.uhuruphotos.foundation.result.api.SimpleResult
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.EffectHandler
import com.savvasdalkitsis.uhuruphotos.foundation.strings.api.R.string
import com.savvasdalkitsis.uhuruphotos.foundation.upload.api.usecase.UploadUseCase
import com.savvasdalktsis.uhuruphotos.feature.download.domain.api.usecase.DownloadUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import org.joda.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlin.random.Random

internal class LightboxActionsContext @Inject constructor(
    val mediaUseCase: MediaUseCase,
    val downloadUseCase: DownloadUseCase,
    val uploadUseCase: UploadUseCase,
    val personUseCase: PersonUseCase,
    val feedUseCase: FeedUseCase,
    val memoriesUseCase: MemoriesUseCase,
    val searchUseCase: SearchUseCase,
    val localAlbumUseCase: LocalAlbumUseCase,
    val metadataUseCase: MetadataUseCase,
    val userAlbumUseCase: UserAlbumUseCase,
    val autoAlbumUseCase: AutoAlbumUseCase,
    val trashUseCase: TrashUseCase,
    val remoteMediaUseCase: RemoteMediaUseCase,
    @DateModule.DisplayingDateTimeFormat
    val displayingDateTimeFormat: DateTimeFormatter,
    @ApplicationContext
    val context: Context,
    private val localMediaDeletionUseCase: LocalMediaDeletionUseCase,
) {

    var mediaItemType = MediaItemType.default
    val currentMediaId = MutableSharedFlow<MediaId<*>>(1)

    fun deletionCategory(item: SingleMediaItemState) = when {
        mediaItemType == TRASHED -> REMOTE_ITEM_TRASHED
        item.id.isBothRemoteAndLocal -> FULLY_SYNCED_ITEM
        item.id.findLocal != null -> LOCAL_ONLY_ITEM
        else -> REMOTE_ITEM
    }

    suspend fun FlowCollector<LightboxMutation>.deleteLocal(
        mediaItem: SingleMediaItemState
    ): SimpleResult {
        val id = mediaItem.id.findLocal!!
        val result = localMediaDeletionUseCase.deleteLocalMediaItems(
            listOf(
                LocalMediaDeletionRequest(id.value, id.isVideo)
            )
        )
        return when(result) {
            is Error -> Err(result.e)
            is RequiresPermissions -> {
                emit(LightboxMutation.AskForPermissions(result.deniedPermissions))
                Err(MissingPermissionsException(result.deniedPermissions))
            }
            Success -> Ok(Unit)
        }
    }

    fun processAndRemoveMediaItem(
        state: LightboxState,
        effect: EffectHandler<LightboxEffect>,
        process: suspend FlowCollector<LightboxMutation>.() -> SimpleResult,
    ) = processMediaItem(state, effect, process) {
        emit(RemoveMediaItemFromSource(state.currentMediaItem.id))
    }

    fun processMediaItem(
        state: LightboxState,
        effect: EffectHandler<LightboxEffect>,
        process: suspend FlowCollector<LightboxMutation>.() -> SimpleResult,
        postProcessAction: suspend FlowCollector<LightboxMutation>.() -> Unit,
    ) = flow {
        emit(Loading)
        emit(HideAllConfirmationDialogs)
        val result = process()
        emit(FinishedLoading)
        result
            .onFailure {
                log(it)
            }
            .onSuccess {
                postProcessAction()
                if (state.media.size == 1) {
                    effect.handleEffect(NavigateBack)
                }
            }
    }

    suspend fun FlowCollector<LightboxMutation>.loadMediaDetails(
        mediaId: MediaId<*>,
        refresh: Boolean = false,
    ) {
        emit(Loading)
        emit(LoadingDetails(mediaId))
        if (refresh) {
            mediaUseCase.refreshDetailsNow(mediaId)
        } else {
            mediaUseCase.refreshDetailsNowIfMissing(mediaId)
        }.onFailure {
            emit(ShowErrorMessage(string.error_loading_photo_details))
        }
        emit(FinishedLoading)
        emit(FinishedLoadingDetails(mediaId))
    }

    internal suspend fun LightboxActionsContext.cropLocal(
        state: LightboxState,
        effect: EffectHandler<LightboxEffect>
    ) {
        fun SingleMediaItemState.fileName() = localPath?.substringAfterLast("/")
            ?: "PHOTO_${Random.nextInt()}"
        state.currentMediaItem.id.findLocal?.let { media ->
            effect.handleEffect(
                CropPhoto(
                    uri = Uri.parse(media.contentUri),
                    fileName = state.currentMediaItem.fileName(),
                    timestamp = try {
                        displayingDateTimeFormat.parseDateTime(state.currentMediaItem.dateAndTime).millis
                    } catch (e: Exception) {
                        log(e)
                        null
                    },
                )
            )
        }
    }

}
