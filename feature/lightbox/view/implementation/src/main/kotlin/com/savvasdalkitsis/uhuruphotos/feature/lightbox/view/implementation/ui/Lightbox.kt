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
package com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.actions.ChangedToPage
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.seam.actions.LightboxAction
import com.savvasdalkitsis.uhuruphotos.feature.lightbox.view.implementation.ui.state.LightboxState
import com.savvasdalkitsis.uhuruphotos.foundation.ui.api.insets.insetsTop
import com.savvasdalkitsis.uhuruphotos.foundation.ui.api.ui.SheetHandle
import com.savvasdalkitsis.uhuruphotos.foundation.ui.api.ui.SheetSize
import com.savvasdalkitsis.uhuruphotos.foundation.ui.api.window.LocalWindowSize
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun Lightbox(
    state: LightboxState,
    action: (LightboxAction) -> Unit
) {
    LightboxBackPressHandler(state, action)
    val infoSheetState = rememberModalBottomSheetState()
    val sheetSize by SheetSize.rememberSheetSize()
    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.media.size },
    )

    LaunchedEffect(state.currentIndex) {
        pagerState.scrollToPage(state.currentIndex)
    }
    HorizontalPager(
        state = pagerState,
        pageSpacing = 12.dp,
        key = { page -> state.media.getOrNull(page)?.id?.value ?: page.toString() },
        userScrollEnabled = true,
    ) { index ->
        LightboxScaffold(sheetSize, state, index, action)
        if (!state.infoSheetHidden) {
            ModalBottomSheet(
                containerColor = Color.Transparent,
                content = {
                    Spacer(modifier = Modifier.height(insetsTop()))
                    Surface(
                        modifier = Modifier
                            .align(CenterHorizontally)
                            .clip(MaterialTheme.shapes.large)
                            .heightIn(min = max(100.dp, sheetSize.size.height - insetsTop()))
                            .let {
                                when (LocalWindowSize.current.widthSizeClass) {
                                    WindowWidthSizeClass.Compact -> it
                                    else -> it.widthIn(max = 460.dp)
                                }
                            }
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Column(
                            modifier = Modifier
                        ) {
                            SheetHandle()
                            LightboxInfoSheet(
                                state = state,
                                index = index,
                                sheetState = infoSheetState,
                                action = action
                            )
                        }
                    }
                },
                sheetState = infoSheetState,
                onDismissRequest = {},
            )
        }
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            action(ChangedToPage(page))
        }
    }
}