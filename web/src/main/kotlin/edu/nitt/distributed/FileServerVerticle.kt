package edu.nitt.distributed

import io.vertx.kotlin.core.VertxOptions
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition

class FileServerVerticle : AbstractVerticle() {
    override fun start() {
        val clusterManager = IgniteClusterManager()
        ipAddressFlowable.subscribe({
            val options = VertxOptions().setClusterManager(clusterManager).setClusterPort(20000).setClusterHost(it.hostAddress)
            Vertx.rxClusteredVertx(options)
                .subscribe({ vertx ->
                    vertx.rxExecuteBlocking<Ignite> { Ignition.start() }
                        .subscribe { ignite -> println("Ignite node started successfully") }
                    val router: Router = Router.router(vertx)
                    router.route("/eventbus/*").handler(eventBusHandler(vertx))
                    vertx.createHttpServer().requestHandler({ router.accept(it) }).listen(11123)
                })
        }, { println("Application failed to start on local node") })
    }
}