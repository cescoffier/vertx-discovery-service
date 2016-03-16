package io.vertx.ext.msa.abcd;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.circuitbreaker.CircuitBreaker;
import io.vertx.ext.circuitbreaker.CircuitBreakerOptions;
import io.vertx.ext.discovery.DiscoveryOptions;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.rest.DiscoveryRestEndpoint;
import io.vertx.ext.discovery.types.HttpEndpoint;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.Map;

/**
 * A publishes a HTML page and a rest service that call B.
 */
public class A extends AbstractVerticle {

  protected Record registration;
  protected HttpClient client;
  protected DiscoveryService discovery;
  protected CircuitBreaker circuitBreaker;

  @Override
  public void start(Future<Void> future) throws Exception {
    discovery = DiscoveryService.create(vertx, new DiscoveryOptions());
    circuitBreaker = CircuitBreaker.create(name(), vertx,
        new CircuitBreakerOptions()
            .setMaxFailures(1)
            .setTimeoutInMs(1000)
            .setResetTimeoutInMs(2000)
            .setFallbackOnFailure(true))
        .openHandler(v -> {
          System.out.println("Circuit opened");
        })
        .halfOpenHandler(v -> {
          // Reset the client, so next call will retrieve the record.
          System.out.println("Attempting reset");
          if (client != null) {
            client.close();
            client = null;
          }
        });

    Router router = Router.router(vertx);

    // Publish assets
    router.route("/assets/*").handler(StaticHandler.create("assets"));

    DiscoveryRestEndpoint.create(router, discovery, "/registry");

    // Publish the A service
    router.route(HttpMethod.GET, "/A").handler(this::handleRequest);

    router.route(HttpMethod.GET, "/env").handler(context -> {
      Map env = System.getenv();
      JsonObject json = new JsonObject(env);
      context.response().putHeader("content-type", "application/json").end(json.encodePrettily());
    });

    vertx.createHttpServer().requestHandler(router::accept).listen(port(), ar -> {
      if (ar.succeeded()) {
        Record record = HttpEndpoint.createRecord(name(), "localhost", port(), "/A");
        discovery.publish(record, registered -> {
          if (registered.succeeded()) {
            System.out.println(name() + " registered");
            this.registration = record;
            future.complete();
          } else {
            future.fail("Registration of " + name() + " failed " + registered.cause());
          }
        });
      } else {
        future.fail("Cannot start the HTTP server " + ar.cause());
      }
    });
  }

  @Override
  public void stop(Future future) throws Exception {
    discovery.unpublish(registration.getRegistration(), result -> {
      if (result.failed()) {
        future.fail(result.cause());
      }
      future.complete();
    });
  }

  public void handleRequest(RoutingContext context) {
    String param = context.request().getParam("name");

    if (client == null) {
      discovery.getRecord(new JsonObject().put("name", dependency()), ar -> {
        if (ar.succeeded() && ar.result() != null) {
          client = DiscoveryService.getService(vertx, ar.result()).get();
          client.getNow("/?name=" + param, response -> {
            response.bodyHandler(buffer -> {
              context.response().setStatusCode(200).end(buffer.toString() + message(param));
            });
          });
        } else {
          // No record
          context.response().setStatusCode(200).end("no service available" + message(param));
        }
      });
    } else {
      circuitBreaker.executeAsynchronousCodeWithFallback(future -> {
        client.getNow("/?name=" + param, response -> {
          response.bodyHandler(buffer -> {
            context.response().setStatusCode(200).end(buffer.toString() + message(param));
            future.complete();
          });
        });
      }, v -> {
        context.response().setStatusCode(200).end("no service available" + message(param));
      });
    }
  }

  public int port() {
    return 8080;
  }

  public String name() {
    return "A";
  }

  public String dependency() {
    return "B";
  }

  public String message(String param) {
    return "\n" + "Hello " + param;
  }
}
