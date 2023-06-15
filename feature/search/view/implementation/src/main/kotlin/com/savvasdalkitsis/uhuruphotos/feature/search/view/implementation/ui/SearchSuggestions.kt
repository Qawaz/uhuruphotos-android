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
package com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.seam.actions.SearchAction
import com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.ui.state.SearchState
import com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.ui.state.SearchSuggestion

@Composable
internal fun SearchSuggestions(
    state: SearchState,
    action: (SearchAction) -> Unit,
    onSearchAction: (String) -> Unit,
) {
    Popup {
        Card(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .animateContentSize()
            ) {
                for (suggestion in state.searchSuggestions) {
                    when (suggestion) {
                        is SearchSuggestion.RecentSearchSuggestion -> recentSearchSuggestion(
                            suggestion = suggestion.query,
                            action = action
                        ) {
                            onSearchAction(suggestion.query)
                        }
                        is SearchSuggestion.ServerSearchSuggestion -> serverSearchSuggestion(
                            suggestion = suggestion.query
                        ) {
                            onSearchAction(suggestion.query)
                        }
                        is SearchSuggestion.PersonSearchSuggestion -> personSuggestion(
                            suggestion.person,
                            action,
                        )
                    }
                }
            }
        }
    }
}