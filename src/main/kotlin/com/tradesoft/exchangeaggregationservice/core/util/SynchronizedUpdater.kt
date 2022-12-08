package com.tradesoft.exchangeaggregationservice.core.util

class SynchronizedUpdater {
    fun updateSynchronized(update: () -> Unit) {
        synchronized(this) {
            update()
        }
    }
}
