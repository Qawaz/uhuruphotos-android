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

package com.savvasdalkitsis.uhuruphotos.foundation.notification.api

import android.content.BroadcastReceiver
import android.content.Context
import androidx.annotation.StringRes
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.savvasdalkitsis.uhuruphotos.math.toProgressPercent

abstract class ForegroundNotificationWorker<BR>(
    context: Context,
    params: WorkerParameters,
    private val foregroundInfoBuilder: ForegroundInfoBuilder,
    @StringRes private val notificationTitle: Int,
    private val notificationId: Int,
    private val notificationChannelId: String = NotificationChannels.Jobs.id,
    private val cancelBroadcastReceiver: Class<BR>? = null,
) : CoroutineWorker(context, params) where BR: BroadcastReceiver {

    private val notificationManager = NotificationManagerCompat.from(applicationContext)

    final override suspend fun doWork(): Result {
        setForeground(createForegroundInfo(0))
        updateProgress(0)
        return work()
    }

    abstract suspend fun work(): Result

    suspend fun updateProgress(current: Int, max: Int) {
        updateProgress(current.toProgressPercent(max), "$current/$max")
    }

    suspend fun updateProgress(progress: Int, text: String? = null) {
        setProgress(workDataOf(Progress to progress))

        notificationManager.notify(notificationId, foregroundInfoBuilder.buildNotification(
            applicationContext,
            notificationTitle,
            notificationChannelId,
            progress,
            text,
            cancelBroadcastReceiver,
        ))
    }

    override suspend fun getForegroundInfo(): ForegroundInfo = createForegroundInfo(null)

    private fun createForegroundInfo(progress: Int?) = foregroundInfoBuilder.build(
        applicationContext,
        notificationTitle,
        notificationId,
        notificationChannelId,
        progress,
    )

    companion object {
        private const val Progress = "Progress"

        fun getProgressOf(work: WorkInfo) = work.progress.getInt(Progress, 0)
    }
}