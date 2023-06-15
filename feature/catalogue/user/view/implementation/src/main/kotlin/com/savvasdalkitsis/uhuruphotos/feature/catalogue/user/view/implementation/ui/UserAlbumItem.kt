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
package com.savvasdalkitsis.uhuruphotos.feature.catalogue.user.view.implementation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.savvasdalkitsis.uhuruphotos.feature.catalogue.user.view.api.state.UserAlbumState
import com.savvasdalkitsis.uhuruphotos.feature.catalogue.user.view.implementation.seam.UserAlbumSelected
import com.savvasdalkitsis.uhuruphotos.feature.catalogue.user.view.implementation.seam.UserAlbumsAction
import com.savvasdalkitsis.uhuruphotos.feature.media.common.domain.api.model.MediaId
import com.savvasdalkitsis.uhuruphotos.feature.media.common.domain.api.model.MediaItemInstance
import com.savvasdalkitsis.uhuruphotos.feature.media.common.view.api.ui.Cel
import com.savvasdalkitsis.uhuruphotos.feature.media.common.view.api.ui.Vitrine
import com.savvasdalkitsis.uhuruphotos.feature.media.common.view.api.ui.state.toCel
import com.savvasdalkitsis.uhuruphotos.foundation.strings.api.R

@Composable
internal fun UserAlbumItem(
    album: UserAlbumState,
    action: (UserAlbumsAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .clickable { action(UserAlbumSelected(album)) }
    ) {
        if (album.cover.hasMoreThanOneItem) {
            Vitrine(
                state = album.cover,
                onSelected = {
                    action(UserAlbumSelected(album))
                },
                shape = MaterialTheme.shapes.large,
            )
        } else {
            Cel(
                state = album.cover.cel1 ?: MediaItemInstance(
                    id = MediaId.Remote("", false, ""),
                    mediaHash = "",
                ).toCel(),
                onSelected = {
                    action(UserAlbumSelected(album))
                },
                aspectRatio = 1f,
                contentScale = ContentScale.Crop,
                shape = MaterialTheme.shapes.large,
            )
        }
        Text(
            text = album.title.toText(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall
                .copy(fontWeight = FontWeight.Bold),
        )
        album.photoCount?.let {
            Text(
                text = pluralStringResource(R.plurals.item_count, it, it),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}