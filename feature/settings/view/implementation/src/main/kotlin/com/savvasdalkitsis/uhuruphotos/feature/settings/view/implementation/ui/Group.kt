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
package com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.ui.controller.SettingsGroupState
import com.savvasdalkitsis.uhuruphotos.foundation.icons.api.R.drawable

@Composable
internal fun Group(
    groupState: SettingsGroupState,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.padding(4.dp),
        shadowElevation = 4.dp,
    ) {
        Column {
            val arrowAngle = remember { Animatable(180f) }
            Box(modifier = Modifier
                .fillMaxWidth()
                .clickable { groupState.toggleCollapsed() }
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(groupState.title),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Icon(
                        modifier = Modifier.rotate(arrowAngle.value),
                        painter = painterResource(id = drawable.ic_arrow_up),
                        contentDescription = null,
                    )
                }
            }
            AnimatedVisibility(visible = !groupState.isCollapsed) {
                Column {
                    content()
                }
            }
            LaunchedEffect(groupState.isCollapsed) {
                arrowAngle.animateTo(
                    when {
                        groupState.isCollapsed -> 180f
                        else -> 0f
                    }
                )
            }
        }
    }
}