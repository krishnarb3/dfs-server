package edu.nitt.distributed

import io.reactivex.Flowable

import io.vertx.reactivex.core.RxHelper
import io.vertx.reactivex.core.Vertx

class ConsumerService(vertx: Vertx) {

    val scheduler = RxHelper.blockingScheduler(vertx)
    val dfsStore = DfsStore(scheduler)

    fun messageConsumer(messageFlowable: Flowable<Any>): Flowable<EventBusResponse> {
        return messageFlowable.map { EventBusResponse("message", it) }
    }

    fun createOrJoinConsumer(roomFlowable: Flowable<Any>): Flowable<EventBusResponse> {
        return roomFlowable.flatMap {
            val room = it.toString()
            dfsStore.get(room).map { peers ->
                when (peers) {
                    0 -> {
                        dfsStore.put(room, 1).subscribe({ println("Initiated room: $room") })
                        EventBusResponse("$room.created", room)
                    }
                    1 -> {
                        dfsStore.put(room, 2).subscribe({ println("Completed room: $room") })
                        EventBusResponse("$room.joined", room)
                        EventBusResponse("$room.ready", room)
                    }
                    else -> EventBusResponse("$room error", room)
                }
            }
        }
    }

    fun byeConsumer(byeFlowable: Flowable<Any>): Flowable<EventBusResponse> {
        return byeFlowable.map { EventBusResponse("bye", "bye") }
    }

    fun ipConsumer(): Flowable<EventBusResponse> {
        return ipAddressFlowable.map { EventBusResponse("ipaddr", it.hostAddress) }
    }
}
