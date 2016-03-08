package io.vertx.ext.discovery.rest;

import com.jayway.restassured.response.Response;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.discovery.*;
import io.vertx.ext.discovery.impl.DiscoveryClient;
import io.vertx.ext.discovery.types.EventBusService;
import io.vertx.ext.service.HelloService;
import io.vertx.ext.service.HelloServiceImpl;
import io.vertx.ext.web.Router;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class DiscoveryRestEndpointTest {

  protected Vertx vertx;
  protected DiscoveryService discovery;
  private HttpServer http;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    discovery = new DiscoveryClient(vertx, new DiscoveryOptions());

    Router router = Router.router(vertx);
    DiscoveryRestEndpoint.create(router, discovery);

    AtomicBoolean done = new AtomicBoolean();
    http = vertx.createHttpServer().requestHandler(router::accept).listen(8080, ar -> {
      done.set(ar.succeeded());
    });

    await().untilAtomic(done, is(true));
  }

  @After
  public void tearDown() {
    discovery.close();

    AtomicBoolean completed = new AtomicBoolean();
    http.close(ar -> {
      completed.set(true);
    });
    await().untilAtomic(completed, is(true));

    completed.set(false);
    vertx.close((v) -> completed.set(true));
    await().untilAtomic(completed, is(true));
  }

  @Test
  public void testThatWeGetThePublishedServices() {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));

    discovery.publish(record, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);

    Response response = get("/discovery");
    JsonArray services = new JsonArray(response.asString());

    assertThat(services.size()).isEqualTo(1);
    Record rec = new Record(services.getJsonObject(0));
    assertThat(rec.getStatus()).isEqualTo(Status.UP);
    assertThat(rec.getRegistration()).isNotNull();
    assertThat(rec.getName()).isEqualTo("Hello");

    AtomicBoolean done = new AtomicBoolean();
    discovery.unpublish(record.getRegistration(), ar -> {
      done.set(true);
    });
    await().untilAtomic(done, is(true));

    response = get("/discovery");
    services = new JsonArray(response.asString());

    assertThat(services.size()).isEqualTo(0);

  }

  @Test
  public void testThatWeGetTheTwoPublishedServicesWithMetadata() {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");

    Record record1 = EventBusService.createRecord("Hello", HelloService.class, "address",
        new JsonObject().put("key", "foo"));
    Record record2 = EventBusService.createRecord("Hello-2", HelloService.class, "address",
        new JsonObject().put("key", "bar"));

    discovery.publish(record1, (r) -> { });
    discovery.publish(record2, (r) -> { });

    await().until(() -> record1.getRegistration() != null);
    await().until(() -> record2.getRegistration() != null);

    Response response = get("/discovery");
    JsonArray services = new JsonArray(response.asString());

    assertThat(services.size()).isEqualTo(2);

    for(Object json : services) {
      Record rec = new Record((JsonObject) json);
      assertThat(rec.getStatus()).isEqualTo(Status.UP);
      assertThat(rec.getRegistration()).isNotNull();
      assertThat(rec.getName()).startsWith("Hello");
      assertThat(rec.getMetadata().getString("key")).isNotNull();

      get("/discovery/" + rec.getRegistration()).then().body("name", not(nullValue()));
    }
  }

  @Test
  public void testPublicationAndUnpublicationFromTheRestAPI() {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));

    Response response1 = given().request().body(record.toJson().toString()).post("/discovery");
    assertThat(response1.getStatusCode()).isEqualTo(201);
    String reg = new JsonObject(response1.asString()).getString("registration");
    assertThat(reg).isNotNull();

    Response response = get("/discovery");
    JsonArray services = new JsonArray(response.asString());

    assertThat(services.size()).isEqualTo(1);
    Record rec = new Record(services.getJsonObject(0));
    assertThat(rec.getStatus()).isEqualTo(Status.UP);
    assertThat(rec.getRegistration()).isEqualTo(reg);
    assertThat(rec.getName()).isEqualTo("Hello");

    Response response2 = delete("/discovery/" + reg);
    assertThat(response2.statusCode()).isEqualTo(204);

    response = get("/discovery");
    services = new JsonArray(response.asString());

    assertThat(services.size()).isEqualTo(0);

    // Missing...
    response2 = delete("/discovery/" + reg);
    assertThat(response2.statusCode()).isEqualTo(500);
  }

  @Test
  public void testUpdate() throws UnsupportedEncodingException {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));

    discovery.publish(record, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);

    Record retrieved = retrieve(record.getRegistration());
    assertThat(retrieved.getStatus()).isEqualTo(Status.UP);

    retrieved.setStatus(Status.OUT_OF_SERVICE).getMetadata().put("foo", "bar");

    Response response = given().body(retrieved.toJson().toString()).put("/discovery/" + record.getRegistration());
    assertThat(response.getStatusCode()).isEqualTo(200);
    retrieved = new Record(new JsonObject(response.asString()));

    assertThat(retrieved.getStatus()).isEqualTo(Status.OUT_OF_SERVICE);
    assertThat(retrieved.getMetadata().getString("foo")).isEqualTo("bar");

    // Check that we cannot find the service without specifying the Status
    response = get("/discovery/");
    JsonArray services = new JsonArray(response.asString());
    assertThat(services.size()).isEqualTo(0);

    String encoded = URLEncoder.encode("{\"status\":\"*\"}", "UTF-8");
    response = get("/discovery/?query=" + encoded);
    services = new JsonArray(response.asString());
    assertThat(services.size()).isEqualTo(1);
  }

  @Test
  public void testLookupWithQuery() throws UnsupportedEncodingException {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");

    Record record1 = EventBusService.createRecord("Hello", HelloService.class, "address",
        new JsonObject().put("key", "foo"));
    Record record2 = EventBusService.createRecord("Hello-2", HelloService.class, "address",
        new JsonObject().put("key", "bar"));

    discovery.publish(record1, (r) -> { });
    discovery.publish(record2, (r) -> { });

    await().until(() -> record1.getRegistration() != null);
    await().until(() -> record2.getRegistration() != null);

    String encoded = URLEncoder.encode("{\"name\":\"Hello\"}", "UTF-8");
    Response response = get("/discovery?query=" + encoded);
    JsonArray services = new JsonArray(response.asString());

    assertThat(services.size()).isEqualTo(1);
  }

  @Test
  public void testLookupWithNonMatchingQuery() throws UnsupportedEncodingException {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");

    Record record1 = EventBusService.createRecord("Hello", HelloService.class, "address",
        new JsonObject().put("key", "foo"));
    Record record2 = EventBusService.createRecord("Hello-2", HelloService.class, "address",
        new JsonObject().put("key", "bar"));

    discovery.publish(record1, (r) -> { });
    discovery.publish(record2, (r) -> { });

    await().until(() -> record1.getRegistration() != null);
    await().until(() -> record2.getRegistration() != null);

    String encoded = URLEncoder.encode("{\"stuff\":\"*\"}", "UTF-8");
    Response response = get("/discovery?query=" + encoded);
    JsonArray services = new JsonArray(response.asString());

    assertThat(services.size()).isEqualTo(0);
  }

  @Test
  public void testFailedPublication() {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setRegistration("this-is-not-allowed")
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));

    Response response = given().request().body(record.toJson().toString()).post("/discovery");
    assertThat(response.getStatusCode()).isEqualTo(500);
  }

  @Test
  public void testRetrievingMissingRecord() {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));

    discovery.publish(record, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);

    Record retrieved = retrieve(record.getRegistration());
    assertThat(retrieved.getStatus()).isEqualTo(Status.UP);

    // Unregister it
    Response response2 = delete("/discovery/" + record.getRegistration());
    assertThat(response2.statusCode()).isEqualTo(204);

    Response response = get("/discovery/" + record.getRegistration());
    assertThat(response.getStatusCode()).isEqualTo(404);
  }

  @Test
  public void testUpdateWithUUIDMismatch() throws UnsupportedEncodingException {
    HelloService svc = new HelloServiceImpl("stuff");
    ProxyHelper.registerService(HelloService.class, vertx, svc, "address");
    Record record = new Record()
        .setName("Hello")
        .setLocation(new JsonObject().put(Record.ENDPOINT, "address"));

    discovery.publish(record, (r) -> {
    });
    await().until(() -> record.getRegistration() != null);

    Record retrieved = retrieve(record.getRegistration());
    assertThat(retrieved.getStatus()).isEqualTo(Status.UP);

    retrieved.setStatus(Status.OUT_OF_SERVICE).setRegistration("not-the-right-one").getMetadata().put("foo", "bar");

    Response response = given().body(retrieved.toJson().toString()).put("/discovery/" + record.getRegistration());
    assertThat(response.getStatusCode()).isEqualTo(400);
  }

  private Record retrieve(String uuid) {
    return new Record(new JsonObject(get("/discovery/" + uuid).asString()));
  }

}