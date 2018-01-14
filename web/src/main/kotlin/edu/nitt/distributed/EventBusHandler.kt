package edu.nitt.distributed

import io.reactivex.Flowable
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.RxHelper
import io.vertx.reactivex.core.eventbus.EventBus
import io.vertx.kotlin.ext.web.handler.sockjs.BridgeOptions
import io.vertx.kotlin.ext.web.handler.sockjs.PermittedOptions
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler

import edu.nitt.distributed.EventBusConsumer

fun eventBusHandler(vertx: Vertx): SockJSHandler {

    val eventBus: EventBus = vertx.eventBus()
    val eventBusConsumer = EventBusConsumer(RxHelper.blockingScheduler(vertx))

    eventBusConsumer.messageConsumer(eventBus.addrToFlowable("message"))
            .subscribe({ eventBus.send(it.address, it.response) }, { println(it.message) })

    eventBusConsumer.ipConsumer(eventBus.addrToFlowable("ipaddr"))
            .subscribe({ eventBus.publish(it.address, it.response) }, { println(it.message) })

    eventBusConsumer.createOrJoinConsumer(eventBus.addrToFlowable("create or join"))
            .subscribe({
                println("Create or join")
                println(eventBusConsumer.dfsStore.get("peers"))
                eventBus.publish(it.address, it.response)
            }, { println(it.message) })

    eventBusConsumer.byeConsumer(eventBus.addrToFlowable("bye"))
            .subscribe({ eventBus.publish(it.address, it.response) }, { println(it.message) })

    return SockJSHandler.create(vertx).bridge(BridgeOptions()
            .addInboundPermitted(PermittedOptions().setAddressRegex(".*"))
            .addOutboundPermitted(PermittedOptions().setAddressRegex(".*")))
}

fun EventBus.addrToFlowable(address: String): Flowable<Any> = this.consumer<Any>(address).bodyStream().toFlowable()