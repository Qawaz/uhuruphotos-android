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
package com.savvasdalkitsis.uhuruphotos.feature.stats.view.implementation.seam

import com.savvasdalkitsis.uhuruphotos.feature.stats.domain.api.model.CountryVisit
import com.savvasdalkitsis.uhuruphotos.feature.stats.domain.api.model.Year
import com.savvasdalkitsis.uhuruphotos.feature.stats.view.implementation.ui.state.StatsState
import com.savvasdalkitsis.uhuruphotos.foundation.seam.api.Mutation
import kotlinx.collections.immutable.toPersistentList

sealed class StatsMutation(
    mutation: Mutation<StatsState>,
) : Mutation<StatsState> by mutation {

    data class ShowMediaPerYear(val mediaByYear: Map<Year, Int>) : StatsMutation({
        it.copy(
            isLoadingMediaByYear = false,
            mediaByYear = mediaByYear,
        )
    })

    data class ShowTimeline(val timeline: List<CountryVisit>) : StatsMutation({
        it.copy(
            isLoadingTimeline = false,
            timeline = timeline.toPersistentList(),
        )
    })

    data object Loading : StatsMutation({
        it.copy(isLoadingMediaByYear = true)
    })
}