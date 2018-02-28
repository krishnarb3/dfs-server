package edu.nitt.distributed

import io.reactivex.Flowable
import io.reactivex.Scheduler

import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition

data class DfsStore(val scheduler: Scheduler) {

    private val cache: IgniteCache<String, Int> =
            Ignition.ignite().getOrCreateCache<String, Int>("dfs")

    fun put(key: String, value: Int): Flowable<Unit?> =
        Flowable.just(key).observeOn(scheduler).map { cache.put(key, value) }

    fun get(key: String): Flowable<Int> = Flowable.just(key).observeOn(scheduler).map { cache.get(key) ?: 0 }

}