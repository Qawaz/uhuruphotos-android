package com.savvasdalkitsis.librephotos.window

import androidx.compose.runtime.compositionLocalOf

enum class WindowSizeClass {
    COMPACT, MEDIUM, EXPANDED
}

object WindowSize {
   val LOCAL_WIDTH = compositionLocalOf { WindowSizeClass.COMPACT }
   val LOCAL_HEIGHT = compositionLocalOf { WindowSizeClass.COMPACT }
}