package io.vertx.ext.discovery.impl;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.*;
import io.vertx.ext.service.HelloService;
import io.vertx.ext.service.HelloServiceImpl;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DiscoveryClientTest {


  protected Vertx vertx;
  protected DiscoveryService discovery;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    discovery = new DiscoveryClient(vertx, new DiscoveryOptions());
  }

  @After
  public void tearDown() {
    discovery.close();
    AtomicBoolean completed = new AtomicBoolean();
    vertx.close((v) -> completed.set(true));
    await().untilAtomic(completed, is(true));
  }

  @Test
  public void testPublicationAndSimpleLookup() {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));

    discovery.publish(record, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);

    AtomicReference<Record> found = new AtomicReference<>();
    discovery.getRecord(new JsonObject().put("name", "Hello"), ar -> {
      found.set(ar.result());
    });

    await().until(() -> found.get() != null);
    assertThat(found.get().getLocation().getString("endpoint")).isEqualTo("address");

    AtomicBoolean done = new AtomicBoolean();
    discovery.unpublish(record.getRegistration(), v -> {
      done.set(v.succeeded());
    });

    await().untilAtomic(done, is(true));

    found.set(null);
    done.set(false);
    discovery.getRecord(new JsonObject().put("name", "Hello"), ar -> {
      found.set(ar.result());
      done.set(true);
    });

    await().untilAtomic(done, is(true));
    assertThat(found.get()).isNull();
  }

  @Test
  public void testPublicationAndFilteredLookup() {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setMetadata(new JsonObject().put("key", "A"))
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));
    Record record2 = new Record()
        .setName("Hello-2")
        .setMetadata(new JsonObject().put("key", "B"))
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address2"));
    discovery.publish(record, (r) -> {
    });
    discovery.publish(record2, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);
    await().until(() -> record2.getRegistration() != null);

    AtomicReference<Record> found = new AtomicReference<>();
    discovery.getRecord(new JsonObject().put("key", "A"), ar -> {
      found.set(ar.result());
    });
    await().until(() -> found.get() != null);
    assertThat(found.get().getLocation().getString("endpoint")).isEqualTo("address");

    found.set(null);
    discovery.getRecord(new JsonObject().put("key", "B"), ar -> {
      found.set(ar.result());
    });
    await().until(() -> found.get() != null);
    assertThat(found.get().getLocation().getString("endpoint")).isEqualTo("address2");

    found.set(null);
    AtomicBoolean done = new AtomicBoolean();
    discovery.getRecord(new JsonObject().put("key", "C"), ar -> {
      found.set(ar.result());
      done.set(true);
    });
    await().untilAtomic(done, is(true));
    assertThat(found.get()).isNull();

    found.set(null);
    done.set(false);
    discovery.getRecord(new JsonObject().put("key", "B").put("foo", "bar"), ar -> {
      found.set(ar.result());
      done.set(true);
    });
    await().untilAtomic(done, is(true));
    assertThat(found.get()).isNull();
  }

  @Test
  public void testAnnounce() {
    List<Record> announces = new ArrayList<>();

    vertx.eventBus().consumer(DiscoveryOptions.DEFAULT_ANNOUNCE_ADDRESS,
        msg -> announces.add(new Record((JsonObject) msg.body())));

    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setMetadata(new JsonObject().put("key", "A"))
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));
    Record record2 = new Record()
        .setName("Hello-2")
        .setMetadata(new JsonObject().put("key", "B"))
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address2"));
    discovery.publish(record, (r) -> {
    });
    discovery.publish(record2, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);
    await().until(() -> record2.getRegistration() != null);

    await().until(() -> announces.size() == 2);
    for (Record rec : announces) {
      assertThat(rec.getStatus()).isEqualTo(Status.UP);
    }

    discovery.unpublish(record2.getRegistration(), v -> {

    });

    await().until(() -> announces.size() == 3);
    assertThat(announces.get(2).getStatus()).isEqualTo(Status.DOWN);
  }

}