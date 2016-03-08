package io.vertx.ext.msa.abcd;

import io.vertx.core.Launcher;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class A extends SimpleHttpService {


  public static void main(String[] args) {
    Launcher.executeCommand("run", "-cluster", A.class.getName(), "--instances=2");
  }


  @Override
  public int port() {
    return 8083;
  }

  @Override
  public String name() {
    return "A";
  }

  @Override
  public String dependency() {
    return "B";
  }

  public String message(String param) {
    return "\n" + "Hello " + param;
  }
}
