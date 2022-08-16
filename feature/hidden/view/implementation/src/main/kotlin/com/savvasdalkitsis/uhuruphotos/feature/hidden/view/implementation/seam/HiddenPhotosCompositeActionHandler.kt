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
package com.savvasdalkitsis.uhuruphotos.feature.hidden.view.implementation.seam

import com.savvasdalkitsis.uhuruphotos.api.gallery.page.seam.GalleryPageAction
import com.savvasdalkitsis.uhuruphotos.api.gallery.page.seam.GalleryPageEffect
import com.savvasdalkitsis.uhuruphotos.api.gallery.page.ui.state.GalleryPageState
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.ActionHandler
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.CompositeActionHandler
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.Either
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.Mutation
import javax.inject.Inject

internal class HiddenPhotosCompositeActionHandler @Inject constructor(
    hiddenPhotosAlbumPageActionHandler: HiddenPhotosAlbumPageActionHandler,
    hiddenPhotosActionHandler: HiddenPhotosActionHandler,
) : ActionHandler<
        Pair<GalleryPageState, HiddenPhotosState>,
        Either<GalleryPageEffect, HiddenPhotosEffect>,
        Either<GalleryPageAction, HiddenPhotosAction>,
        Mutation<Pair<GalleryPageState, HiddenPhotosState>>
        > by CompositeActionHandler(
    handler1 = hiddenPhotosAlbumPageActionHandler,
    handler2 = hiddenPhotosActionHandler
)