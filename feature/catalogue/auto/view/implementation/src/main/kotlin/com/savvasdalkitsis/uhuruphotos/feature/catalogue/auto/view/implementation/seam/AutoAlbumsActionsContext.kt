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
package com.savvasdalkitsis.uhuruphotos.feature.catalogue.auto.view.implementation.seam

import com.savvasdalkitsis.uhuruphotos.feature.catalogue.auto.domain.api.usecase.AutoAlbumsUseCase
import com.savvasdalkitsis.uhuruphotos.feature.catalogue.auto.view.implementation.seam.effects.AutoAlbumsEffect
import com.savvasdalkitsis.uhuruphotos.feature.catalogue.auto.view.implementation.seam.effects.ErrorLoadingAlbums
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.EffectHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

internal class AutoAlbumsActionsContext @Inject constructor(
    val autoAlbumsUseCase: AutoAlbumsUseCase,
) {

    val loading = MutableSharedFlow<Boolean>()

    suspend fun refreshAlbums(effect: EffectHandler<AutoAlbumsEffect>) {
        loading.emit(true)
        val result = autoAlbumsUseCase.refreshAutoAlbums()
        if (result.isFailure) {
            effect.handleEffect(ErrorLoadingAlbums)
        }
        // delaying to give ui time to receive the new albums before
        // dismissing the loading bar since no albums logic relies on that
        delay(500)
        loading.emit(false)
    }
}
