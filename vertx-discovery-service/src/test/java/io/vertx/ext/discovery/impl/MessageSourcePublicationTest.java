package io.vertx.ext.discovery.impl;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.DiscoveryOptions;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.Service;
import io.vertx.ext.discovery.types.EventBusService;
import io.vertx.ext.discovery.types.MessageSource;
import io.vertx.ext.service.HelloService;
import io.vertx.ext.service.HelloServiceImpl;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class MessageSourcePublicationTest {

  private Vertx vertx;
  private DiscoveryService discovery;

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
  public void test() throws InterruptedException {
    Random random = new Random();
    vertx.setPeriodic(10, l -> {
      vertx.eventBus().publish("data", random.nextDouble());
    });

    Record record = MessageSource.createRecord("Hello", "data");

    discovery.publish(record, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);

    AtomicReference<Record> found = new AtomicReference<>();
    discovery.getRecord(new JsonObject().put("name", "Hello"), ar -> {
      found.set(ar.result());
    });

    await().until(() -> found.get() != null);

    Service service = DiscoveryService.getService(vertx, found.get());
    MessageConsumer<Double> consumer = service.get();

    List<Double> data = new ArrayList<>();
    consumer.handler(message -> {
      data.add(message.body());
    });
    await().until(() -> ! data.isEmpty());
    service.release();
    int size = data.size();
    Thread.sleep(200);
    assertThat(data.size()).isEqualTo(size);
  }
}
