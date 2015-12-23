package io.vertx.ext.service.discovery;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.service.discovery.utils.ClassLoaderUtils;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@ProxyGen
@VertxGen
public interface DiscoveryService {

  void publish(String serviceItf, JsonObject properties, String address, Handler<AsyncResult<String>> resultHandler);

  void unpublish(String id, Handler<AsyncResult<Void>> resultHandler);

  void getService(String serviceItf, JsonObject filter, Handler<AsyncResult<JsonObject>> resultHandler);

  void getServices(String serviceItf, JsonObject filter, Handler<AsyncResult<List<JsonObject>>> resultHandler);

  @GenIgnore
  static <T> T getService(Class<T> itf, Vertx vertx, JsonObject reference) {
    return ProxyHelper.createProxy(itf, vertx, reference.getString("service.address"));
  }

  @GenIgnore
  static <T> T getService(Vertx vertx, JsonObject reference) {
    return getService(vertx, reference, Thread.currentThread().getContextClassLoader());
  }

  @GenIgnore
  static <T> T getService(Vertx vertx, JsonObject reference, ClassLoader classLoader) {
    Class<T> itf = ClassLoaderUtils.load(reference.getString("service.interface"), classLoader);
    if (itf == null) {
      throw new IllegalStateException("Cannot load class " + reference.getString("service.interface"));
    } else {
      return getService(itf, vertx, reference);
    }
  }

}
