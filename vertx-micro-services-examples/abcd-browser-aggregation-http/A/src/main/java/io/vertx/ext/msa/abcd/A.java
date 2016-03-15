package io.vertx.ext.msa.abcd;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

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

    router.route(HttpMethod.GET, "/endpoints").handler(context -> {
      String b = "http://" + System.getenv("B_APP_PORT").replace("tcp", "http");
      //TODO c and d.
      JsonObject result = new JsonObject()
          .put("B", b);
      context.response().putHeader("content-type", "application/json").end(result.encode());
    });

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}
