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
package com.savvasdalkitsis.uhuruphotos.feature.avatar.domain.implementation.usecase

import com.savvasdalkitsis.uhuruphotos.feature.auth.domain.api.usecase.ServerUseCase
import com.savvasdalkitsis.uhuruphotos.feature.avatar.domain.api.usecase.AvatarUseCase
import com.savvasdalkitsis.uhuruphotos.feature.avatar.view.api.ui.state.AvatarState
import com.savvasdalkitsis.uhuruphotos.feature.avatar.view.api.ui.state.SyncState.BAD
import com.savvasdalkitsis.uhuruphotos.feature.avatar.view.api.ui.state.SyncState.GOOD
import com.savvasdalkitsis.uhuruphotos.feature.avatar.view.api.ui.state.SyncState.IN_PROGRESS
import com.savvasdalkitsis.uhuruphotos.feature.jobs.domain.api.model.JobStatus
import com.savvasdalkitsis.uhuruphotos.feature.jobs.domain.api.model.JobStatus.Failed
import com.savvasdalkitsis.uhuruphotos.feature.jobs.domain.api.model.JobStatus.InProgress
import com.savvasdalkitsis.uhuruphotos.feature.jobs.domain.api.model.JobsStatus
import com.savvasdalkitsis.uhuruphotos.feature.jobs.domain.api.usecase.JobsUseCase
import com.savvasdalkitsis.uhuruphotos.feature.user.domain.api.usecase.UserUseCase
import com.savvasdalkitsis.uhuruphotos.feature.welcome.domain.api.usecase.WelcomeUseCase
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import se.ansman.dagger.auto.AutoBind
import javax.inject.Inject

@AutoBind
@ActivityRetainedScoped
class AvatarUseCase @Inject constructor(
    private val userUseCase: UserUseCase,
    private val serverUseCase: ServerUseCase,
    private val jobsUseCase: JobsUseCase,
    private val welcomeUseCase: WelcomeUseCase,
) : AvatarUseCase {

    override fun getAvatarState(): Flow<AvatarState> = welcomeUseCase.flow(
        withoutRemoteAccess = jobsUseCase.observeJobsStatusFilteredBySettings().map { jobsStatus ->
            AvatarState(syncState = jobsStatus.syncState())
        },
        withRemoteAccess = combine(
            userUseCase.observeUser(),
            jobsUseCase.observeJobsStatusFilteredBySettings(),
            serverUseCase.observeServerUrl(),
        ) { user, jobsStatus, serverUrl ->
            AvatarState(
                avatarUrl = user.avatar?.let { "$serverUrl$it" },
                syncState = jobsStatus.syncState(),
                initials = user.firstName.initial() + user.lastName.initial(),
                userFullName = "${user.firstName} ${user.lastName}",
                serverUrl = serverUrl,
            )
        },
    )

    private fun JobsStatus.syncState() = jobs.values.run {
        when {
            any { it is Failed } -> BAD
            any { it is InProgress } -> IN_PROGRESS
            else -> GOOD
        }
    }

    private fun String?.initial() =
        orEmpty().firstOrNull()?.toString()?.uppercase() ?: ""
}