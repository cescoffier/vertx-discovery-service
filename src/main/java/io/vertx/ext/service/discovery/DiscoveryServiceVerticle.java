package io.vertx.ext.service.discovery;

import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.Lock;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DiscoveryServiceVerticle extends AbstractVerticle implements DiscoveryService {

  private String nominalAddress;

  @Override
  public void start() throws Exception {
    nominalAddress = config().getString("registry.address",
        "vertx.registry");
    ProxyHelper.registerService(DiscoveryService.class, vertx, this, nominalAddress);
    // Announce the service
    vertx.eventBus().publish(nominalAddress + ".announce", new JsonObject()
        .put("event", "arrival")
        .put("registry.address", nominalAddress));
  }

  @ProxyClose
  public void close() {
    vertx.eventBus().publish(nominalAddress + ".announce", new JsonObject()
        .put("event", "departure")
        .put("registry.address", nominalAddress));
  }

  @Override
  public void publish(String serviceItf, JsonObject properties, String address, Handler<AsyncResult<String>> resultHandler) {
    Objects.requireNonNull(serviceItf);
    Objects.requireNonNull(address);
    Objects.requireNonNull(resultHandler);

    String uuid = UUID.randomUUID().toString();
    if (properties == null) {
      properties = new JsonObject();
    }
    JsonObject registration = properties.copy()
        .put("service.interface", serviceItf)
        .put("service.id", uuid)
        .put("service.address", address);

    acquireLock(resultHandler, lock -> getRegistry(lock, resultHandler, map ->
        vertx.<String>executeBlocking(
            future -> {
              JsonObject old = map.putIfAbsent(address, registration);
              if (old != null) {
                future.fail("Address already registered with service id : " + old.getString("service.id"));
              } else {
                future.complete(uuid);
              }
            },
            id -> {
              resultHandler.handle(id);
              lock.release();
            }
        )));
  }

  @Override
  public void unpublish(String id, Handler<AsyncResult<Void>> resultHandler) {
    Objects.requireNonNull(id);
    if (resultHandler == null) {
      resultHandler = (v) -> {
      };
    }
    Handler<AsyncResult<Void>> handler = resultHandler;

    acquireLock(resultHandler, lock -> getRegistry(lock, handler,
        map -> vertx.<Void>executeBlocking(
            future -> {
              Optional<JsonObject> match = map.values().stream().filter(reg -> reg.getString("service.id").equals(id))
                  .findFirst();
              if (match.isPresent()) {
                map.remove(match.get().getString("service.address"));
                future.complete();
              } else {
                future.fail("Service registration not found");
              }
            },
            result -> {
              handler.handle(result);
              lock.release();
            }
        )));
  }

  @Override
  public void getService(String serviceItf, JsonObject filter, Handler<AsyncResult<JsonObject>> resultHandler) {
    Objects.requireNonNull(resultHandler);

    JsonObject query;
    if (filter == null) {
      query = new JsonObject();
    } else {
      query = filter.copy();
    }
    if (serviceItf != null) {
      query.put("service.interface", serviceItf);
    }

    acquireLock(resultHandler, lock -> getRegistry(lock, resultHandler,
        map ->
            vertx.<JsonObject>executeBlocking(
                future -> {
                  Optional<JsonObject> found = map.values().stream().filter(reg -> match(reg, query)).findAny();
                  if (found.isPresent()) {
                    future.complete(found.get());
                  } else {
                    future.fail("No matching service found");
                  }
                },
                result -> {
                  resultHandler.handle(result);
                  lock.release();
                }
            )
    ));
  }

  @Override
  public void getServices(String serviceItf, JsonObject filter, Handler<AsyncResult<List<JsonObject>>> resultHandler) {
    Objects.requireNonNull(resultHandler);

    JsonObject query;
    if (filter == null) {
      query = new JsonObject();
    } else {
      query = filter.copy();
    }
    if (serviceItf != null) {
      query.put("service.interface", serviceItf);
    }

    acquireLock(resultHandler, lock -> getRegistry(lock, resultHandler,
        map ->
            vertx.<List<JsonObject>>executeBlocking(
                future -> {
                  List<JsonObject> matches = map.values().stream().filter(reg -> match(reg, query)).collect
                      (Collectors.toList());
                  future.complete(matches);
                },
                result -> {
                  resultHandler.handle(result);
                  lock.release();
                }
            )
    ));
  }

  public static boolean match(JsonObject registration, JsonObject query) {
    // A service registration match <=> all values from the query matches.
    // If the query as a value set to "*" any value are accepted
    for (Map.Entry<String, Object> entry : query) {
      Object value = registration.getValue(entry.getKey());
      if (value != null) {
        if (!entry.getValue().equals("*") && !value.equals(entry.getValue())) {
          return false;
        }
      }
    }
    return true;
  }

  private void acquireLock(Handler resultHandler, Handler<Lock> next) {
    vertx.sharedData().getLock("service.registry.lock", maybeLock -> {
      if (maybeLock.failed()) {
        resultHandler.handle(Future.failedFuture("Cannot acquire registry lock"));
      } else {
        next.handle(maybeLock.result());
      }
    });
  }

  private void getRegistry(Lock lock, Handler resultHandler, Handler<Map<String, JsonObject>> next) {
    vertx.<Map<String, JsonObject>>executeBlocking(future -> {
      Map<String, JsonObject> map = ((VertxInternal) vertx).getClusterManager()
          .getSyncMap("service.registry");
      future.complete(map);
    }, map -> {
      if (map.succeeded()) {
        next.handle(map.result());
      } else {
        resultHandler.handle(Future.failedFuture(map.cause()));
        lock.release();
      }
    });
  }


}
