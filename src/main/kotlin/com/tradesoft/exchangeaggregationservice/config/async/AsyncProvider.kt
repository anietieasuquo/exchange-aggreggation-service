package com.tradesoft.exchangeaggregationservice.config.async

import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.RejectedExecutionHandler
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit.SECONDS
import kotlin.coroutines.CoroutineContext

@Component
class AsyncProvider(private val threadPoolProperties: ThreadPoolProperties) {
    private final val rejectedExecutionHandler =
        RejectedExecutionHandler { runnable: Runnable, executor: ThreadPoolExecutor ->
            run {
                if (!executor.isShutdown && !executor.isTerminated && !executor.isTerminating) {
                    val executorService = createExecutor()
                    executorService.execute(runnable)
                }
            }
        }

    private fun createExecutor(): ThreadPoolExecutor = ThreadPoolExecutor(
        threadPoolProperties.corePoolSize,
        threadPoolProperties.maxPoolSize,
        threadPoolProperties.keepAliveTimeInSeconds,
        SECONDS,
        SynchronousQueue()
    )

    private fun getExecutor(): ExecutorService {
        val executor = createExecutor()
        executor.rejectedExecutionHandler = rejectedExecutionHandler
        return executor
    }

    private fun createAsyncExecutor(): ThreadPoolTaskExecutor {
        val taskExecutor = ThreadPoolTaskExecutor().apply {
            corePoolSize = threadPoolProperties.corePoolSize
            maxPoolSize = threadPoolProperties.maxPoolSize
            keepAliveSeconds = threadPoolProperties.keepAliveTimeInSeconds.toInt()
            queueCapacity = threadPoolProperties.queueCapacity
        }
        taskExecutor.setThreadNamePrefix(threadPoolProperties.threadNamePrefix)
        taskExecutor.initialize()
        return taskExecutor
    }


    fun provideAsyncTaskExecutor(): AsyncTaskExecutor {
        val executor = ConcurrentTaskExecutor()
        executor.setConcurrentExecutor(createAsyncExecutor())
        return executor
    }

    fun provideDefaultCoroutineDispatcher(): CoroutineContext = getExecutor().asCoroutineDispatcher()

}
