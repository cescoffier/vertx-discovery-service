package io.vertx.ext.msa.abcd;

import io.vertx.core.*;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class B extends SimpleHttpService {

  public static void main(String[] args) {
    Launcher.executeCommand("run", "-cluster", B.class.getName(), "--instances=2");
  }

  @Override
  public int port() {
    return 8082;
  }

  @Override
  public String name() {
    return "B";
  }

  @Override
  public String dependency() {
    return "C";
  }

  public String message(String param) {
    return "\n" + "Hola " + param;
  }
}
