package com.savvasdalkitsis.librephotos.search.navigation

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavGraphBuilder
import coil.ImageLoader
import com.savvasdalkitsis.librephotos.navigation.ControllersProvider
import com.savvasdalkitsis.librephotos.navigation.navigationTarget
import com.savvasdalkitsis.librephotos.search.mvflow.SearchAction
import com.savvasdalkitsis.librephotos.search.mvflow.SearchEffect
import com.savvasdalkitsis.librephotos.search.view.Search
import com.savvasdalkitsis.librephotos.search.view.state.SearchState
import com.savvasdalkitsis.librephotos.search.viewmodel.SearchEffectsHandler
import com.savvasdalkitsis.librephotos.search.viewmodel.SearchViewModel
import javax.inject.Inject

class SearchNavigationTarget @Inject constructor(
    private val controllersProvider: ControllersProvider,
    private val imageLoader: ImageLoader,
) {

    @ExperimentalComposeUiApi
    fun NavGraphBuilder.create() {
        navigationTarget<SearchState, SearchAction, SearchEffect, SearchViewModel>(
            name = name,
            effects = SearchEffectsHandler(),
            viewBuilder = { state, actions, _ ->
                Search(state, actions, controllersProvider, imageLoader)
            },
            controllersProvider = controllersProvider,
        )
    }

    companion object {
        const val name = "search"
    }
}