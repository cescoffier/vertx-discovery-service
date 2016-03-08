package io.vertx.ext.discovery.types;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.Service;
import io.vertx.ext.discovery.spi.ServiceType;

import java.util.Objects;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class HttpEndpoint implements ServiceType {

  public static final String TYPE = "http-endpoint";

  @Override
  public String name() {
    return TYPE;
  }

  @Override
  public Service get(Vertx vertx, Record record) {
    return new HttpAPI(vertx, record);
  }

  public static Record createRecord(String name, String host, int port, String root, JsonObject metadata) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(host);
    Objects.requireNonNull(root);
    Record record = new Record().setName(name)
        .setType(TYPE)
        .setLocation(new HttpLocation().setHost(host).setPort(port).setRoot(root).toJson());

    if (metadata != null) {
      record.setMetadata(metadata);
    }

    return record;
  }

  public static Record createRecord(String name, String host, int port, String root) {
    return createRecord(name, host, port, root, null);
  }

  public static Record createRecord(String name, String host) {
     return createRecord(name, host, 80, "/", null);
  }

  private class HttpAPI implements Service {

    private final Vertx vertx;
    private final HttpLocation location;
    private HttpClient client;

    public HttpAPI(Vertx vertx, Record record) {
      this.vertx = vertx;
      this.location = new HttpLocation(record.getLocation());
    }

    @Override
    public synchronized <T> T get() {
      if (client != null) {
        return (T) client;
      } else {
        client = vertx.createHttpClient(
            new HttpClientOptions().setDefaultPort(location.getPort())
                .setDefaultHost(location.getHost()));
        return (T) client;
      }
    }

    @Override
    public synchronized void release() {
       client.close();
    }
  }
}
