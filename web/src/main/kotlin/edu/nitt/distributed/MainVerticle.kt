package edu.nitt.distributed

import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.web.Router


class MainVerticle: AbstractVerticle() {
    override fun start() {
        val router: Router = Router.router(vertx)
        router.route("/eventbus/*").handler(eventBusHandler(vertx))
        vertx.createHttpServer().requestHandler({ router.accept(it) }).listen(11123)
    }
}