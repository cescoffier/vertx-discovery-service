package io.vertx.ext.discovery;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.service.HelloService;
import io.vertx.ext.service.HelloServiceImpl;
import io.vertx.ext.service.discovery.DiscoveryService;
import io.vertx.ext.service.discovery.DiscoveryServiceVerticle;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.serviceproxy.ProxyHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class SimpleDiscoveryTest {

  private DiscoveryService discoveryService;
  private Vertx node;

  @Before
  public void setUp(TestContext context) {
    Async async = context.async();

    Vertx.clusteredVertx(new VertxOptions().setClusterHost("127.0.0.1"), ar -> {
      node = ar.result();
      ar.result().deployVerticle(DiscoveryServiceVerticle.class.getName(), dd -> {
        context.assertTrue(dd.succeeded());
        async.complete();
        discoveryService = ProxyHelper.createProxy(DiscoveryService.class, ar.result(), "vertx.registry");
      });
    });
  }

  @After
  public void tearDown(TestContext context) {
    Async async = context.async();
    node.close(done -> {
      async.complete();
    });
  }


  @Test
  public void testRegistrationAndQuery(TestContext context) {
    Async async = context.async();
    Vertx.clusteredVertx(new VertxOptions().setClustered(true).setClusterHost("127.0.0.1"), ar -> {
      Vertx vertx = ar.result();
      HelloServiceImpl impl = new HelloServiceImpl();
      impl.start(vertx, "hello");

      discoveryService.publish(HelloService.class.getName(), null, "hello",
          done ->
              discoveryService.getService(HelloService.class.getName(), null,
                  svc -> {
                    context.assertNotNull(svc.result());
                    HelloService service = DiscoveryService.getService(HelloService.class, vertx, svc.result());
                    service.hello("vert.x", r -> {
                      context.assertEquals(r.result(), "Hello vert.x");

                      // Unregister the service
                      discoveryService.unpublish(done.result(), v -> {
                        impl.stop();
                        discoveryService.getService(HelloService.class.getName(), null, none -> {
                          context.assertFalse(none.succeeded());
                          vertx.close(rr -> async.complete());
                        });
                      });
                    });
                  }));
    });
  }

  @Test
  public void testSelection(TestContext context) {
    Async async = context.async();
    Vertx.clusteredVertx(new VertxOptions().setClustered(true).setClusterHost("127.0.0.1"), ar -> {
      Vertx vertx = ar.result();
      HelloServiceImpl impl = new HelloServiceImpl();
      impl.start(vertx, "hello");

      HelloServiceImpl impl2 = new HelloServiceImpl("Bonjour");
      impl2.start(vertx, "bonjour");

      discoveryService.publish(HelloService.class.getName(), new JsonObject().put("lg", "english"), "hello",
          reg1 -> discoveryService.publish(HelloService.class.getName(), new JsonObject().put("lg", "french"), "bonjour",
              reg2 -> {
                // Get french service
                discoveryService.getService(HelloService.class.getName(), new JsonObject().put("lg", "french"), ref -> {
                  context.assertTrue(ref.succeeded());
                  HelloService service = DiscoveryService.getService(HelloService.class, vertx, ref.result());
                  service.hello("vert.x", result -> {
                    context.assertEquals(result.result(), "Bonjour vert.x");
                    discoveryService.unpublish(reg1.result(),
                        ureg1 -> discoveryService.unpublish(reg2.result(),
                            ureg2 -> vertx.close(closing -> async.complete())));
                  });
                });
              }));
    });
  }

}
