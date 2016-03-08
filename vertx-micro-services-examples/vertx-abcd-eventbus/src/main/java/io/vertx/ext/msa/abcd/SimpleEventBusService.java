package io.vertx.ext.msa.abcd;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.circuitbreaker.CircuitBreaker;
import io.vertx.ext.circuitbreaker.CircuitBreakerOptions;
import io.vertx.ext.discovery.DiscoveryOptions;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.types.EventBusService;
import io.vertx.ext.msa.abcd.service.HelloService;
import io.vertx.serviceproxy.ProxyHelper;


/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public abstract class SimpleEventBusService extends AbstractVerticle implements HelloService {

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
          // Reset the service, so next call will retrieve the record.
          System.out.println("Attempting reset");
          if (service != null) {
            EventBusService.release(service);
            service = null;
          }
        });

    ProxyHelper.registerService(HelloService.class, vertx, this, name());
    Record record = EventBusService.createRecord(name(), HelloService.class, name());
    discovery.publish(record, registered -> {
      if (registered.succeeded()) {
        System.out.println(name() + " registered");
        this.registration = record;
        future.complete();
      } else {
        future.fail("Registration of " + name() + " failed " + registered.cause());
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

  public abstract String name();

  public abstract String dependency();

  public abstract String message(String param);

  @Override
  public void say(String name, Handler<AsyncResult<String>> resultHandler) {
      if (service == null) {
        EventBusService.<HelloService>get(vertx, discovery, new JsonObject().put("name", dependency()), ar -> {
          if (ar.failed()  || ar.result() == null) {
            replyNoServiceAvailable(name, resultHandler);
          } else {
            service = ar.result();
            service.say(name, result -> {
              resultHandler.handle(Future.succeededFuture(result.result() + message(name)));
            });
          }
        });
      } else {
        circuitBreaker.executeAsynchronousCodeWithFallback(future -> {
          service.say(name, result -> {
            if (result.succeeded()) {
              resultHandler.handle(Future.succeededFuture(result.result() + message(name)));
            } else {
              replyNoServiceAvailable(name, resultHandler);
            }
            future.complete();
          });
        }, v -> {
          replyNoServiceAvailable(name, resultHandler);
        });
      }
  }

  private void replyNoServiceAvailable(String name, Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(Future.succeededFuture("no service available" + message(name)));
  }
}
