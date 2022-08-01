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
package com.savvasdalkitsis.implementation.mediastore.service.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

sealed class MediaStoreServiceItem(
    open val id: Long,
    open val displayName: String,
    open val dateAdded: Long,
    open val bucketId: Int,
    open val bucketName: String,
    open val width: Int?,
    open val height: Int?,
    open val size: Int?,
    open val contentUri: Uri,
) {

    data class Photo(
        override val id: Long,
        override val displayName: String,
        override val dateAdded: Long,
        override val bucketId: Int,
        override val bucketName: String,
        override val width: Int?,
        override val height: Int?,
        override val size: Int?,
    ): MediaStoreServiceItem(id, displayName, dateAdded, bucketId, bucketName, width, height, size,
        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id,)
    )

    data class Video(
        override val id: Long,
        override val displayName: String,
        override val dateAdded: Long,
        override val bucketId: Int,
        override val bucketName: String,
        override val width: Int?,
        override val height: Int?,
        override val size: Int?,
        val duration: Int?,
    ): MediaStoreServiceItem(id, displayName, dateAdded, bucketId, bucketName, width, height, size,
        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id,)
    )

}
