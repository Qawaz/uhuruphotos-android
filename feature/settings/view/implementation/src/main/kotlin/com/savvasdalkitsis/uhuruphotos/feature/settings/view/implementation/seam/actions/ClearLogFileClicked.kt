package com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.actions

import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.SettingsActionsContext
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.SettingsEffect
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.SettingsEffect.ShowMessage
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.seam.SettingsMutation
import com.savvasdalkitsis.uhuruphotos.feature.settings.view.implementation.ui.state.SettingsState
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.EffectHandler
import com.savvasdalkitsis.uhuruphotos.foundation.strings.api.R
import kotlinx.coroutines.flow.flow

object ClearLogFileClicked : SettingsAction() {
    context(SettingsActionsContext) override fun handle(
        state: SettingsState,
        effect: EffectHandler<SettingsEffect>
    ) = flow<SettingsMutation> {
        feedbackUseCase.clearLogs()
        effect.handleEffect(ShowMessage(R.string.logs_cleared))
    }

}