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
package com.savvasdalkitsis.uhuruphotos.feature.catalogue.auto.view.implementation.navigation

import androidx.compose.runtime.Composable
import com.savvasdalkitsis.uhuruphotos.feature.catalogue.auto.view.api.navigation.AutoAlbumsNavigationRoute
import com.savvasdalkitsis.uhuruphotos.feature.catalogue.auto.view.implementation.ui.AutoAlbums
import com.savvasdalkitsis.uhuruphotos.feature.catalogue.auto.view.implementation.viewmodel.AutoAlbumsViewModel
import com.savvasdalkitsis.uhuruphotos.feature.settings.domain.api.usecase.SettingsUseCase
import com.savvasdalkitsis.uhuruphotos.foundation.navigation.api.NavigationTarget
import com.savvasdalkitsis.uhuruphotos.foundation.navigation.api.NavigationTargetBuilder
import com.savvasdalkitsis.uhuruphotos.foundation.navigation.api.NavigationTargetRegistry
import se.ansman.dagger.auto.AutoInitialize
import javax.inject.Inject
import javax.inject.Singleton

@AutoInitialize
@Singleton
class AutoAlbumsNavigationTarget @Inject constructor(
    private val settingsUseCase: SettingsUseCase,
    private val navigationTargetBuilder: NavigationTargetBuilder,
    registry: NavigationTargetRegistry,
) : NavigationTarget<AutoAlbumsNavigationRoute>(AutoAlbumsNavigationRoute::class, registry) {

    @Composable
    override fun View(route: AutoAlbumsNavigationRoute) = with(navigationTargetBuilder) {
        ViewModelView(
            themeMode = settingsUseCase.observeThemeModeState(),
            route = route,
            viewModel = AutoAlbumsViewModel::class,
        ) { state, actions ->
            AutoAlbums(
                state = state,
                action = actions,
            )
        }
    }
}