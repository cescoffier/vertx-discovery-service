package io.vertx.ext.discovery.types;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.Service;
import io.vertx.ext.discovery.spi.ServiceType;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MessageSource implements ServiceType {

  public static final String TYPE = "message-source";

  @Override
  public String name() {
    return TYPE;
  }

  @Override
  public Service get(Vertx vertx, Record record) {
    return new Source(vertx, record);
  }

  public static Record createRecord(String name, String address, Class type, JsonObject metadata) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(address);
    Record record = new Record().setName(name)
        .setType(TYPE)
        .setLocation(new JsonObject().put(Record.ENDPOINT, address));

    if (metadata != null) {
      record.setMetadata(metadata);
    }

    if (type != null) {
      record.setMetadata(new JsonObject().put("message.type", type.getName()));
    }

    return record;
  }


  public static Record createRecord(String name, String address, Class type) {
     return createRecord(name, address, type, null);
  }

  public static Record createRecord(String name, String address) {
    return createRecord(name, address, null);
  }

  private class Source implements Service {

    private final Record record;
    private final Vertx vertx;
    private MessageConsumer consumer;

    public Source(Vertx vertx, Record record) {
      this.vertx = vertx;
      this.record = record;
    }

    @Override
    public synchronized <T> T get() {
      if (consumer != null) {
        return (T) consumer;
      }
      consumer = vertx.eventBus().consumer(record.getLocation().getString(Record.ENDPOINT));
      return  (T) consumer;
    }

    @Override
    public synchronized void release() {
       consumer.unregister();
    }
  }
}
