package io.vertx.ext.discovery.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.spi.cluster.ClusterManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class ExtendedAsyncMapImpl<K, V> implements ExtendedAsyncMap<K, V> {


  private final Vertx vertx;
  private final Map<K, V> syncMap;

  public ExtendedAsyncMapImpl(Vertx vertx, String name) {
    this.vertx = vertx;
    ClusterManager clusterManager = ((VertxInternal) vertx).getClusterManager();
    if (clusterManager == null) {
      syncMap = new ConcurrentHashMap<>();
    } else {
      syncMap = clusterManager.getSyncMap(name);
    }
  }


  @Override
  public void getAll(Handler<AsyncResult<Map<K, V>>> asyncResultHandler) {
    vertx.<Map<K,V>>executeBlocking(
        future ->  {
          Map<K, V> map = new LinkedHashMap<>();
          syncMap.entrySet().stream().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
          future.complete(map);
        },
        asyncResultHandler
    );
  }

  @Override
  public void getAll(Set<K> keys, Handler<AsyncResult<Map<K, V>>> asyncResultHandler) {
    vertx.<Map<K,V>>executeBlocking(
        future ->  {
          Map<K, V> map = new LinkedHashMap<>();
          syncMap.entrySet().stream().forEach(entry -> {
            if (keys.contains(entry.getKey())) {
              map.put(entry.getKey(), entry.getValue());
            }
          });
          future.complete(map);
        },
        asyncResultHandler
    );
  }

  @Override
  public void keySet(Handler<AsyncResult<Set<K>>> asyncResultHandler) {
    vertx.<Set<K>>executeBlocking(
        future -> future.complete(syncMap.keySet()),
        asyncResultHandler
    );
  }

  @Override
  public void values(Handler<AsyncResult<List<V>>> asyncResultHandler) {
    vertx.<List<V>>executeBlocking(
        future -> future.complete(new ArrayList<>(syncMap.values())),
        asyncResultHandler
    );
  }

  @Override
  public void get(K k, Handler<AsyncResult<V>> handler) {
    vertx.<V>executeBlocking(
        future -> future.complete(syncMap.get(k)),
        handler
    );
  }

  @Override
  public void put(K k, V v, Handler<AsyncResult<Void>> handler) {
    vertx.<Void>executeBlocking(
        future -> {
          syncMap.put(k, v);
          future.complete();
        },
        handler
    );
  }

  @Override
  public void put(K k, V v, long l, Handler<AsyncResult<Void>> handler) {
    throw new UnsupportedOperationException("Insertion with timeout is not supported");
  }

  @Override
  public void putIfAbsent(K k, V v, Handler<AsyncResult<V>> handler) {
    vertx.<V>executeBlocking(
        future -> future.complete(syncMap.putIfAbsent(k, v)),
        handler
    );
  }

  @Override
  public void putIfAbsent(K k, V v, long l, Handler<AsyncResult<V>> handler) {
    throw new UnsupportedOperationException("Insertion with timeout is not supported");
  }

  @Override
  public void remove(K k, Handler<AsyncResult<V>> handler) {
    vertx.<V>executeBlocking(
        future -> future.complete(syncMap.remove(k)),
        handler
    );
  }

  @Override
  public void removeIfPresent(K k, V v, Handler<AsyncResult<Boolean>> handler) {
    vertx.<Boolean>executeBlocking(
        future -> future.complete(syncMap.remove(k, v)),
        handler
    );
  }

  @Override
  public void replace(K k, V v, Handler<AsyncResult<V>> handler) {
    vertx.<V>executeBlocking(
        future -> future.complete(syncMap.replace(k, v)),
        handler
    );
  }

  @Override
  public void replaceIfPresent(K k, V v, V v1, Handler<AsyncResult<Boolean>> handler) {
    vertx.<Boolean>executeBlocking(
        future -> future.complete(syncMap.replace(k, v, v1)),
        handler
    );
  }

  @Override
  public void clear(Handler<AsyncResult<Void>> handler) {
    vertx.<Void>executeBlocking(
        future -> {
          syncMap.clear();
          future.complete();
        },
        handler
    );
  }

  @Override
  public void size(Handler<AsyncResult<Integer>> handler) {
    vertx.<Integer>executeBlocking(
        future -> future.complete(syncMap.size()),
        handler
    );
  }
}
