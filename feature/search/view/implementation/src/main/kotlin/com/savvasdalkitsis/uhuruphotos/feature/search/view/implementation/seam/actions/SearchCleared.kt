package com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.seam.actions

import com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.seam.SearchActionsContext
import com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.seam.SearchEffect
import com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.seam.SearchMutation.SwitchStateToIdle
import com.savvasdalkitsis.uhuruphotos.feature.search.view.implementation.ui.state.SearchState
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.EffectHandler
import kotlinx.coroutines.flow.flow

object SearchCleared : SearchAction() {

    context(SearchActionsContext) override fun handle(
        state: SearchState,
        effect: EffectHandler<SearchEffect>
    ) = flow {
        lastSearch?.cancel()
        emit(SwitchStateToIdle)
    }
}