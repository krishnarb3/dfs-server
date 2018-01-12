package edu.nitt.distributed

import io.reactivex.Flowable
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.eventbus.EventBus
import io.vertx.kotlin.ext.web.handler.sockjs.BridgeOptions
import io.vertx.kotlin.ext.web.handler.sockjs.PermittedOptions
import io.vertx.reactivex.ext.web.handler.sockjs.SockJSHandler

import edu.nitt.distributed.messageConsumer
import edu.nitt.distributed.ipConsumer
import edu.nitt.distributed.createOrJoinConsumer
import edu.nitt.distributed.byeConsumer

fun eventBusHandler(vertx: Vertx): SockJSHandler {

    val eventBus: EventBus = vertx.eventBus()

    messageConsumer(eventBus.addrToFlowable("message"))
            .subscribe({ eventBus.send(it.address, it.response) }, { println(it.message) })

    ipConsumer(eventBus.addrToFlowable("ipaddr"))
            .subscribe({ eventBus.publish(it.address, it.response) }, { println(it.message) })

    createOrJoinConsumer(eventBus.addrToFlowable("create or join"))
            .subscribe({ eventBus.publish(it.address, it.response) }, { println(it.message) })

    byeConsumer(eventBus.addrToFlowable("bye"))
            .subscribe({ eventBus.publish(it.address, it.response) }, { println(it.message) })

    return SockJSHandler.create(vertx).bridge(BridgeOptions()
            .addInboundPermitted(PermittedOptions().setAddressRegex(".*"))
            .addOutboundPermitted(PermittedOptions().setAddressRegex(".*")))
}

fun EventBus.addrToFlowable(address: String): Flowable<Any> = this.consumer<Any>(address).bodyStream().toFlowable()