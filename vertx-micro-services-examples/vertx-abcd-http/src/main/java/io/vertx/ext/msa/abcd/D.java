package io.vertx.ext.msa.abcd;

import io.vertx.core.*;
import io.vertx.ext.web.RoutingContext;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class D extends SimpleHttpService {

  public static void main(String[] args) {
    Launcher.executeCommand("run", "-cluster", D.class.getName(), "--instances=2");
  }

  public void handleRequest(RoutingContext context) {
    context.response().setStatusCode(200).end(message(context.request().getParam("name")));
  }

  @Override
  public int port() {
    return 8080;
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
