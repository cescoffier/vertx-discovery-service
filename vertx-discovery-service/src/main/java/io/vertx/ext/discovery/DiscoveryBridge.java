package io.vertx.ext.discovery;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface DiscoveryBridge {

  void start(Vertx vertx, DiscoveryService discovery, JsonObject configuration,
             Handler<AsyncResult<Void>> completionHandler);

  void stop(Vertx vertx, DiscoveryService discovery);

}
