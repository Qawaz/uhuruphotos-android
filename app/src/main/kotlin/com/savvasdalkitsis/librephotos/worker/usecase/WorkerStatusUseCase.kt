package com.savvasdalkitsis.librephotos.worker.usecase

import androidx.lifecycle.LiveData
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.savvasdalkitsis.librephotos.coroutines.onMain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class WorkerStatusUseCase @Inject constructor(
    private val workManager: WorkManager,
) {

    fun monitorUniqueJobStatus(jobName: String): Flow<WorkInfo.State> {
        var observer: ((MutableList<WorkInfo>) -> Unit)? = null
        var liveData: LiveData<MutableList<WorkInfo>>? = null
        return channelFlow {
            observer = {
                val workInfo = it.getOrNull(0)
                workInfo?.state?.let {
                    CoroutineScope(Dispatchers.Default).launch {
                        send(it)
                    }
                }
            }
            liveData = workManager.getWorkInfosForUniqueWorkLiveData(jobName)
            onMain {
                liveData!!.observeForever(observer!!)
            }
            awaitClose {
                onMain {
                    liveData!!.removeObserver(observer!!)
                }
            }
        }.cancellable()
    }
}