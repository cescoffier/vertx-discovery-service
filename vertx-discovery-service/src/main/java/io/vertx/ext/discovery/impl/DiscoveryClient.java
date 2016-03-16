package io.vertx.ext.discovery.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.discovery.*;
import io.vertx.ext.discovery.spi.DiscoveryBackend;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DiscoveryClient implements DiscoveryService {

  private final Vertx vertx;
  private final MessageConsumer<JsonObject> service;
  private final String announce;
  private final DiscoveryBackend backend;

  private final Set<DiscoveryBridge> bridges = new CopyOnWriteArraySet<>();
  private final static Logger LOGGER = LoggerFactory.getLogger(DiscoveryClient.class.getName());


  public DiscoveryClient(Vertx vertx, DiscoveryOptions options) {
    this.vertx = vertx;
    if (options.isExposedAsService()) {
      service = ProxyHelper.registerService(DiscoveryService.class, vertx, this, options.getAddress());
    } else {
      service = null;
    }

    this.announce = options.getAnnounceAddress();

    this.backend = getBackend();
    this.backend.init(vertx);

  }

  private DiscoveryBackend getBackend() {
    ServiceLoader<DiscoveryBackend> backends = ServiceLoader.load(DiscoveryBackend.class);
    Iterator<DiscoveryBackend> iterator = backends.iterator();
    if (!iterator.hasNext()) {
      return new DefaultDiscoveryBackend();
    } else {
      return iterator.next();
    }
  }


  @Override
  public void registerDiscoveryBridge(DiscoveryBridge bridge, JsonObject configuration) {
    vertx.<Void>executeBlocking(
        future -> {
          bridge.start(vertx, this, configuration, (ar) -> {
            if (ar.failed()) {
              future.fail(ar.cause());
            } else {
              bridges.add(bridge);
              future.complete();
            }
          });
        },
        ar -> {
          if (ar.failed()) {
            LOGGER.error("Cannot start the discovery bridge " + bridge, ar.cause());
          } else {
            LOGGER.info("Discovery bridge " + bridge + " started");
          }
        }
    );
  }

  @Override
  public void close() {
    LOGGER.info("Stopping discovery service");
    if (service != null) {
      service.unregister();
    }

    for (DiscoveryBridge bridge : bridges) {
      bridge.stop(vertx, this);
    }
  }

  @Override
  public void publish(Record record, Handler<AsyncResult<Record>> resultHandler) {
    backend.store(record.setStatus(Status.UP), resultHandler);
    Record announcedRecord = new Record(record);
    announcedRecord
        .setRegistration(null)
        .setStatus(Status.UP);
    vertx.eventBus().publish(announce, announcedRecord.toJson());
  }

  @Override
  public void unpublish(String id, Handler<AsyncResult<Void>> resultHandler) {
    backend.remove(id, record -> {
      if (record.failed()) {
        resultHandler.handle(Future.failedFuture(record.cause()));
        return;
      }
      Record announcedRecord = new Record(record.result());
      announcedRecord
          .setRegistration(null)
          .setStatus(Status.DOWN);
      vertx.eventBus().publish(announce, announcedRecord.toJson());
      resultHandler.handle(Future.succeededFuture());
    });

  }

  @Override
  public void getRecord(JsonObject filter,
                        Handler<AsyncResult<Record>> resultHandler) {
    if (filter.getString("status") == null) {
      filter.put("status", Status.UP.name());
    }
    backend.getRecords(list -> {
      if (list.failed()) {
        resultHandler.handle(Future.failedFuture(list.cause()));
      } else {
        Optional<Record> any = list.result().stream()
            .filter(record -> record.match(filter))
            .findAny();
        if (any.isPresent()) {
          resultHandler.handle(Future.succeededFuture(any.get()));
        } else {
          resultHandler.handle(Future.succeededFuture(null));
        }
      }
    });
  }

  @Override
  public void getRecords(JsonObject filter, Handler<AsyncResult<List<Record>>> resultHandler) {
    if (filter.getValue("status") == null) {
      filter.put("status", Status.UP.name());
    }
    backend.getRecords(list -> {
      if (list.failed()) {
        resultHandler.handle(Future.failedFuture(list.cause()));
      } else {
        List<Record> match = list.result().stream()
            .filter(record -> record.match(filter)).collect(Collectors.toList());
        resultHandler.handle(Future.succeededFuture(match));
      }
    });
  }

  @Override
  public void update(Record record, Handler<AsyncResult<Record>> resultHandler) {
    backend.update(record, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(record));
      }
    });

    Record announcedRecord = new Record(record);
    vertx.eventBus().publish(announce, announcedRecord.toJson());
  }
}

