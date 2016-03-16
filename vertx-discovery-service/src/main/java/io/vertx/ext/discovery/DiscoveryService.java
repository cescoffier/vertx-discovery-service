package io.vertx.ext.discovery;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyClose;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.impl.DiscoveryClient;
import io.vertx.ext.discovery.impl.ServiceTypes;

import java.util.List;
import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface DiscoveryService {

  static DiscoveryService create(Vertx vertx, DiscoveryOptions options) {
    return new DiscoveryClient(vertx, options);
  }

  static Service getService(Vertx vertx, Record record) {
    Objects.requireNonNull(record);
    Objects.requireNonNull(vertx);
    return ServiceTypes.get(record).get(vertx, record);
  }

  /**
   * Registers a discovery bridge.
   *
   * @param bridge        the bridge
   * @param configuration the optional configuration
   */
  @GenIgnore
  void registerDiscoveryBridge(DiscoveryBridge bridge, JsonObject configuration);

  @ProxyClose
  void close();

  void publish(Record record, Handler<AsyncResult<Record>> resultHandler);

  void unpublish(String id, Handler<AsyncResult<Void>> resultHandler);

  void getRecord(JsonObject filter, Handler<AsyncResult<Record>> resultHandler);

  void getRecords(JsonObject filter, Handler<AsyncResult<List<Record>>> resultHandler);

  void update(Record record, Handler<AsyncResult<Record>> resultHandler);

}
