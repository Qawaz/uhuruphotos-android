/*
Copyright 2023 Savvas Dalkitsis

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
package com.savvasdalkitsis.uhuruphotos.feature.people.view.implementation.seam.effects

import com.savvasdalkitsis.uhuruphotos.feature.people.view.api.ui.state.Person
import com.savvasdalkitsis.uhuruphotos.feature.people.view.implementation.seam.PeopleEffectsContext
import com.savvasdalkitsis.uhuruphotos.feature.person.view.api.navigation.PersonNavigationRoute

data class NavigateToPerson(val person: Person) : PeopleEffect() {
    context(PeopleEffectsContext) override suspend fun handle() {
        navigator.navigateTo(PersonNavigationRoute(person.id))
    }
}