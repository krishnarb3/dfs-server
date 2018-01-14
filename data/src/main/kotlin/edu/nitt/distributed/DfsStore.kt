package edu.nitt.distributed

import io.reactivex.Flowable
import io.reactivex.Scheduler

import org.apache.ignite.IgniteCache
import org.apache.ignite.Ignition

data class DfsStore(val scheduler: Scheduler) {

    private val cache: IgniteCache<String, String> =
            Ignition.ignite().getOrCreateCache<String, String>("dfs")

    fun put(key: String, value: String) =
        Flowable.just(key).observeOn(scheduler).map { cache.put(key, value) }

    fun get(key: String) = cache.get(key)
}