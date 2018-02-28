package edu.nitt.distributed

import io.vertx.kotlin.core.VertxOptions
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router
import io.vertx.spi.cluster.ignite.IgniteClusterManager

import org.apache.ignite.Ignition

import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers

class MainVerticle : AbstractVerticle() {
    override fun start() {
        val clusterManager = IgniteClusterManager()
        Flowable.just(Ignition.start()).observeOn(Schedulers.io())
            .flatMap { ipAddressFlowable }
            .subscribe({
                val options = VertxOptions().setClusterManager(clusterManager).setClusterHost(it.hostName)
                Vertx.rxClusteredVertx(options)
                    .subscribe({ vertx ->
                        val router: Router = Router.router(vertx)
                        router.route("/eventbus/*").handler(eventBusHandler(vertx))
                        vertx.createHttpServer().requestHandler({ router.accept(it) }).listen(11123)
                    })
            }, { println("Application failed to start on local node") })
    }
}