package io.vertx.ext.discovery.types;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.Service;
import io.vertx.ext.discovery.spi.ServiceType;
import io.vertx.ext.discovery.utils.ClassLoaderUtils;
import io.vertx.serviceproxy.ProxyHelper;

import java.util.Objects;
import java.util.Set;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class EventBusService implements ServiceType {

  public static final String TYPE = "eventbus-service-proxy";

  public static Record createRecord(String name, Class itf, String address, JsonObject metadata) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(itf);
    Objects.requireNonNull(address);

    JsonObject meta;
    if (metadata == null) {
      meta = new JsonObject();
    } else {
      meta = metadata.copy();
    }

    return new Record()
        .setType(TYPE)
        .setName(name)
        .setMetadata(meta.put("service.interface", itf.getName()))
        .setLocation(new JsonObject().put(Record.ENDPOINT, address));
  }

  public static Record createRecord(String name, Class itf, String address) {
    return createRecord(name, itf, address, null);
  }

  public static final Set<Service> BINDINGS = new ConcurrentHashSet<>();

  public static <T> void get(Vertx vertx, DiscoveryService discovery, JsonObject filter, Handler<AsyncResult<T>>
      resultHandler) {
    discovery.getRecord(filter, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        if (ar.result() == null) {
          resultHandler.handle(Future.failedFuture("Cannot find service matching with " + filter));
        } else {
          Service service = DiscoveryService.getService(vertx, ar.result());
          BINDINGS.add(service);
          resultHandler.handle(Future.succeededFuture(service.get()));
        }
      }
    });
  }

  public static <T> void get(Vertx vertx, DiscoveryService discovery, Class<T> itf, Handler<AsyncResult<T>>
      resultHandler) {
    JsonObject filter = new JsonObject().put("service.interface", itf.getName());
    get(vertx, discovery, filter, resultHandler);
  }

  @Override
  public String name() {
    return TYPE;
  }

  @Override
  public Service get(Vertx vertx, Record record) {
    return new EBService(vertx, record);
  }

  public static void release(Object svcObject) {
    for (Service svc : BINDINGS) {
      if (svc.get().equals(svcObject)) {
        BINDINGS.remove(svc);
        return;
      }
    }
  }

  private class EBService implements Service {

    private final Record record;
    private final Vertx vertx;
    private Object proxy;

    public EBService(Vertx vertx, Record record) {
      this.vertx = vertx;
      this.record = record;
    }

    @Override
    public synchronized <T> T get() {
      if (proxy != null) {
        return (T) proxy;
      }

      String className = record.getMetadata().getString("service.interface");
      Objects.requireNonNull(className);
      Class<T> itf = ClassLoaderUtils.load(className, this.getClass().getClassLoader());
      if (itf == null) {
        throw new IllegalStateException("Cannot load class " + className);
      } else {
        T proxy = ProxyHelper.createProxy(itf, vertx, record.getLocation().getString(Record.ENDPOINT));
        this.proxy = proxy;
        return proxy;
      }
    }

    @Override
    public synchronized void release() {
      if (proxy != null) {
        proxy = null;
      }
    }
  }
}
