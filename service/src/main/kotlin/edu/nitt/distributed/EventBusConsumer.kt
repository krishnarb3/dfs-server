package edu.nitt.distributed

import io.reactivex.Flowable
import java.net.Inet6Address
import java.net.NetworkInterface

import io.reactivex.Scheduler

import edu.nitt.distributed.DfsStore

class EventBusConsumer(scheduler: Scheduler) {
    val dfsStore = DfsStore(scheduler)
    var roomList: MutableList<Any> = ArrayList()
    var peers = 0

    fun messageConsumer(messageFlowable: Flowable<Any>): Flowable<EventBusResponse> {
        return messageFlowable.map { EventBusResponse("message", it) }
    }

    fun createOrJoinConsumer(roomFlowable: Flowable<Any>): Flowable<EventBusResponse> {
        return roomFlowable.doOnEach {
                roomList.add(it)
                dfsStore.put("peers", roomList.toString())
            }.map { room ->
            peers++
            when {
                peers == 2 -> {
                    EventBusResponse("joined", room)
                    EventBusResponse("ready", room)
                }
                peers > 2 -> {
                    peers = 1
                    EventBusResponse("created", room)
                }
                else -> EventBusResponse("created", room)
            }
        }
    }

    fun byeConsumer(byeFlowable: Flowable<Any>): Flowable<EventBusResponse> {
        return byeFlowable.map { EventBusResponse("bye", "bye") }
    }

    fun ipConsumer(ipFlowable: Flowable<Any>): Flowable<EventBusResponse> {
        val networkInterfaces = NetworkInterface.getNetworkInterfaces().toList()
        return ipFlowable.flatMap { Flowable.fromIterable(networkInterfaces) }
                .filter { !it.isLoopback && it.isUp }
                .flatMap { Flowable.fromIterable(it.inetAddresses.toList()) }
                .filter { it !is Inet6Address }
                .map { EventBusResponse("ipaddr", it.hostAddress) }
    }

    fun fileConsumer(fileFlowable: Flowable<Any>) {

    }
}