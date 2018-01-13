package edu.nitt.distributed

import io.vertx.kotlin.core.VertxOptions
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router

import io.vertx.spi.cluster.ignite.IgniteClusterManager

class MainVerticle: AbstractVerticle() {
    override fun start() {
        val clusterManager = IgniteClusterManager()
        val options = VertxOptions().setClusterManager(clusterManager)
        Vertx.rxClusteredVertx(options)
            .subscribe({ vertx ->
                val router: Router = Router.router(vertx)
                router.route("/eventbus/*").handler(eventBusHandler(vertx))
                vertx.createHttpServer().requestHandler({ router.accept(it) }).listen(11123)
            })
    }
}