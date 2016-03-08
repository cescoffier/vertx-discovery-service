package io.vertx.ext.discovery.spi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.discovery.Record;

import java.util.List;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface DiscoveryBackend {

  void init(Vertx vertx);

  void store(Record record, Handler<AsyncResult<Record>> resultHandler);

  void remove(Record record, Handler<AsyncResult<Record>> resultHandler);

  void remove(String uuid, Handler<AsyncResult<Record>> resultHandler);

  void update(Record record, Handler<AsyncResult<Void>> resultHandler);

  void getRecords(Handler<AsyncResult<List<Record>>> resultHandler);

  void getRecord(String uuid, Handler<AsyncResult<Record>> resultHandler);

}
