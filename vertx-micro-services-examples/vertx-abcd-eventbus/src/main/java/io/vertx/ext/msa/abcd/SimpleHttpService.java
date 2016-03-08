package io.vertx.ext.msa.abcd;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.circuitbreaker.CircuitBreaker;
import io.vertx.ext.circuitbreaker.CircuitBreakerOptions;
import io.vertx.ext.discovery.DiscoveryOptions;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.rest.DiscoveryRestEndpoint;
import io.vertx.ext.discovery.types.EventBusService;
import io.vertx.ext.discovery.types.HttpEndpoint;
import io.vertx.ext.msa.abcd.service.HelloService;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class SimpleHttpService extends AbstractVerticle {


  protected Record registration;
  protected DiscoveryService discovery;
  protected CircuitBreaker circuitBreaker;
  protected HelloService service;

  @Override
  public void start(Future<Void> future) {
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
          if (service != null) {
            EventBusService.release(service);
            service = null;
          }
        });

    Router router = Router.router(vertx);
    DiscoveryRestEndpoint.create(router, discovery, "/registry");
    router.get("/").handler(this::handleRequest);

    vertx.createHttpServer().requestHandler(router::accept).listen(port(), ar -> {
      if (ar.succeeded()) {
        Record record = HttpEndpoint.createRecord(name(), "localhost", port(), "/");
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
    String name = context.request().getParam("name");

    if (service == null) {
      EventBusService.<HelloService>get(vertx, discovery, new JsonObject().put("name", dependency()), ar -> {
        if (ar.failed() || ar.result() == null) {
          replyNoServiceAvailable(context, name);
        } else {
          service = ar.result();
          service.say(name, result -> {
            context.response().setStatusCode(200).end(result.result() + message(name));
          });
        }
      });
    } else {
      circuitBreaker.executeAsynchronousCodeWithFallback(future -> {
        service.say(name, result -> {
          if (result.succeeded()) {
            context.response().setStatusCode(200).end(result.result() + message(name));
          } else {
            replyNoServiceAvailable(context, name);
          }
          future.complete();
        });
      }, v -> {
        replyNoServiceAvailable(context, name);
      });
    }
  }

  private void replyNoServiceAvailable(RoutingContext context, String name) {
    context.response().setStatusCode(200).end("no service available" + message(name));
  }


  public abstract int port();

  public abstract String name();

  public abstract String dependency();

  public abstract String message(String param);
}
