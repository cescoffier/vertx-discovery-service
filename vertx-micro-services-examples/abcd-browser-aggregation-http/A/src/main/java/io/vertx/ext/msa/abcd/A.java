package io.vertx.ext.msa.abcd;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Map;

/**
 * A publishes a static HTML page that would consume the A, B, C and D Rest services.
 * A also publishes the A REST services and a REST call to retrieve B, C and D addressed.
 * <p>
 * No circuit-breaker involved, as it should be managed in the browser.
 * No discovery involved, as it should be managed in the browser.
 */
public class A extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    Router router = Router.router(vertx);

    // Publish assets
    router.route("/assets/*").handler(StaticHandler.create("assets"));

    // Enable Cors
    router.route().handler(CorsHandler.create("*")
        .allowedMethod(HttpMethod.GET)
        .allowedHeader("Content-Type"));

    // Publish the A service
    router.route(HttpMethod.GET, "/A").handler(context -> {
      context.response().end("Hello " + context.request().getParam("name"));
    });

    router.route(HttpMethod.GET, "/env").handler(context -> {
      Map env = System.getenv();
      JsonObject json = new JsonObject(env);
      context.response().putHeader("content-type", "application/json").end(json.encodePrettily());
    });

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
