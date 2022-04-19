package com.savvasdalkitsis.librephotos.photos.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.ModalBottomSheetValue.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.radusalagean.infobarcompose.InfoBar
import com.radusalagean.infobarcompose.InfoBarMessage
import com.savvasdalkitsis.librephotos.image.view.Image
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction
import com.savvasdalkitsis.librephotos.photos.mvflow.PhotoAction.*
import com.savvasdalkitsis.librephotos.photos.view.state.PhotoState
import com.savvasdalkitsis.librephotos.ui.view.BackPressHandler
import com.savvasdalkitsis.librephotos.ui.view.CommonScaffold
import com.savvasdalkitsis.librephotos.ui.view.FullProgressBar
import com.savvasdalkitsis.librephotos.ui.view.zoom.rememberZoomableState
import com.savvasdalkitsis.librephotos.ui.view.zoom.zoomable
import com.savvasdalkitsis.librephotos.video.view.Video

@Composable
fun Photo(
    state: PhotoState,
    action: (PhotoAction) -> Unit,
) {
    val infoSheetState = rememberModalBottomSheetState(initialValue = Hidden)
    val coroutineScope = rememberCoroutineScope()
    val zoomableState = rememberZoomableState(coroutineScope = coroutineScope)
    var size by remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
    val density = LocalDensity.current

    BackPressHandler {
        if (state.infoSheetState != Hidden) {
            action(HideInfo)
        } else {
            action(NavigateBack)
        }
    }

    ModalBottomSheetLayout(
        sheetShape = RoundedCornerShape(12.dp),
        sheetContent = {
            PhotoDetailsSheet(size, state, action)
        },
        sheetState = infoSheetState
    ) {
        CommonScaffold(
            modifier = Modifier
            .onGloballyPositioned { coordinates ->
                with(density) {
                    size = coordinates.size.toSize().toDpSize()
                }
            },
            title = {},
            toolbarColor = Color.Transparent,
            bottomBarColor = Color.Transparent,
            topBarDisplayed = state.showUI,
            bottomBarDisplayed = state.showUI,
            navigationIcon = {
                IconButton(onClick = { action(NavigateBack) }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "back")
                }
            },
            actionBarContent = {
               PhotoDetailsActionBar(state, action)
            },
            bottomBarContent = {
                PhotoDetailsBottomActionBar(state, action)
            },
        ) { contentPadding ->
            if (state.isLoading && state.lowResUrl.isEmpty()) {
                FullProgressBar()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background)
                        .zoomable(
                            zoomableState = zoomableState,
                            onTap = { action(ToggleUI) },
                            onSwipeAway = { action(NavigateBack) },
                            onSwipeUp = { action(ShowInfo) },
                        )
                ) {
                    when {
                        state.isVideo -> {
                            Video(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.Center),
                                videoUrl = state.fullResUrl,
                                videoThumbnailUrl = state.lowResUrl,
                                play = true,
                            )
                        }
                        else -> Image(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center),
                            lowResUrl = state.lowResUrl,
                            fullResUrl = state.fullResUrl,
                            onFullResImageLoaded = { action(FullImageLoaded) },
                            contentScale = ContentScale.Fit,
                            contentDescription = "photo",
                        )
                    }
                    Column {
                        Spacer(modifier = Modifier.height(contentPadding.calculateTopPadding()))
                        InfoBar(offeredMessage = state.errorMessage?.let { InfoBarMessage(it) }) {
                            action(DismissErrorMessage)
                        }
                    }
                    if (state.showPhotoDeletionConfirmationDialog) {
                        DeletePermissionDialog(
                            photoCount = 1,
                            onDismiss = { action(DismissPhotoDeletionDialog) }
                        ) { action(DeletePhoto) }
                    }
                }
            }
        }
    }
    LaunchedEffect(state.infoSheetState) {
        when (state.infoSheetState) {
            Hidden -> {
                zoomableState.reset()
                infoSheetState.hide()
            }
            Expanded, HalfExpanded -> with (density) {
                if (state.showInfoButton) {
                    zoomableState.animateScaleTo(0.7f)
                    zoomableState.animateOffsetTo(0f, -size.height.toPx() / 4f)
                    infoSheetState.show()
                } else {
                    action(HideInfo)
                    zoomableState.reset()
                }
            }
        }
    }
    if (infoSheetState.currentValue != Hidden) {
        DisposableEffect(Unit) {
            onDispose {
                action(HideInfo)
            }
        }
    }
}