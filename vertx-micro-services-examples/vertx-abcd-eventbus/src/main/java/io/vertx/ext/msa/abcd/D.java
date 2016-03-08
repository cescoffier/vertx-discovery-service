package io.vertx.ext.msa.abcd;

import io.vertx.core.*;
import io.vertx.ext.web.RoutingContext;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class D extends SimpleEventBusService {

  public static void main(String[] args) {
    Launcher.executeCommand("run", "-cluster", D.class.getName(), "--instances=2");
  }

  @Override
  public void say(String name, Handler<AsyncResult<String>> resultHandler) {
    resultHandler.handle(Future.succeededFuture(message(name)));
  }

  @Override
  public String name() {
    return "D";
  }

  @Override
  public String dependency() {
    return "None";
  }

  @Override
  public String message(String param) {
    return "Aloha " + param;
  }
}
