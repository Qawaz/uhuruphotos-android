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
package com.savvasdalkitsis.uhuruphotos.feature.favourites.domain.implementation.usecase

import com.savvasdalkitsis.uhuruphotos.feature.collage.view.api.ui.state.CollageDisplay
import com.savvasdalkitsis.uhuruphotos.feature.collage.view.api.ui.state.PredefinedCollageDisplay
import com.savvasdalkitsis.uhuruphotos.feature.favourites.domain.api.usecase.FavouritesUseCase
import com.savvasdalkitsis.uhuruphotos.foundation.preferences.api.Preferences
import com.savvasdalkitsis.uhuruphotos.foundation.preferences.api.get
import com.savvasdalkitsis.uhuruphotos.foundation.preferences.api.set
import se.ansman.dagger.auto.AutoBind
import javax.inject.Inject

@AutoBind
internal class FavouritesUseCase @Inject constructor(
    private val preferences: Preferences,
) : FavouritesUseCase {

    private val key = "favouriteMediaGalleryDisplay"

    override fun getFavouriteMediaGalleryDisplay(): CollageDisplay =
        preferences.get(key, defaultValue = PredefinedCollageDisplay.default)

    override suspend fun setFavouriteMediaGalleryDisplay(galleryDisplay: PredefinedCollageDisplay) {
        preferences.set(key, galleryDisplay)
    }


}

