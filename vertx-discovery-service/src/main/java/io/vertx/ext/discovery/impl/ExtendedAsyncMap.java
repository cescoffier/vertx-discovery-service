package io.vertx.ext.discovery.impl;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.shareddata.AsyncMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ExtendedAsyncMap<K, V> extends AsyncMap<K, V> {

  void getAll(Handler<AsyncResult<Map<K,V>>> resultHandler);

  void getAll(Set<K> keys, Handler<AsyncResult<Map<K,V>>> resultHandler);

  void keySet(Handler<AsyncResult<Set<K>>> resultHandler);

  void values(Handler<AsyncResult<List<V>>> resultHandler);

}
