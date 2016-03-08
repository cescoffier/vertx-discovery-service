package io.vertx.ext.msa.abcd;

import io.vertx.core.*;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class C extends SimpleHttpService {

  public static void main(String[] args) {
    Launcher.executeCommand("run", "-cluster", C.class.getName(), "--instances=2");
  }

  @Override
  public int port() {
    return 8081;
  }

  @Override
  public String name() {
    return "C";
  }

  @Override
  public String dependency() {
    return "D";
  }

  public String message(String param) {
    return "\n" + "Ola " + param;
  }
}
