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

import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.Q
import android.os.Build.VERSION_CODES.R
import android.provider.MediaStore
import android.provider.MediaStore.Video.Media.DURATION
import android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
import android.provider.MediaStore.Video.Media.getContentUri

internal object MediaVideoColumns {

    val collection: Uri = if (SDK_INT >= Q) {
        getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
        EXTERNAL_CONTENT_URI
    }

    val projection = MediaCommonColumns.projection + arrayOf(
        MediaStore.Video.Media.BUCKET_ID,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
    ) + when {
        SDK_INT >= R -> arrayOf(
            DURATION,
        )
        else -> emptyArray()
    }
}