package edu.nitt.distributed

import io.vertx.kotlin.core.VertxOptions
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.Router
import io.vertx.spi.cluster.ignite.IgniteClusterManager
import org.apache.ignite.Ignite
import org.apache.ignite.Ignition
import org.apache.ignite.configuration.IgniteConfiguration
import java.util.Arrays
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi


class FileServerVerticle : AbstractVerticle() {
    override fun start() {
        ipAddressFlowable.subscribe({
            val igniteConf = staticIpConfig(it.hostAddress)
            val clusterManager = IgniteClusterManager(igniteConf)
            val options = VertxOptions().setClusterManager(clusterManager)
                .setClusterHost(it.hostAddress).setClusterPublicHost(it.hostAddress).setClusterPublicPort(11123)
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

fun staticIpConfig(ipAddress: String): IgniteConfiguration {
    val spi = TcpDiscoverySpi()
    val ipFinder = TcpDiscoveryVmIpFinder()
    ipFinder.setAddresses(Arrays.asList(ipAddress, "127.0.0.1"))
    spi.ipFinder = ipFinder
    val cfg = IgniteConfiguration()
    cfg.discoverySpi = spi
    println(cfg.toString())
    return cfg
}

