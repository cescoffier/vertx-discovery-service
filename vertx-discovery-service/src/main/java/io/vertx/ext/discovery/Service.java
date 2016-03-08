package io.vertx.ext.discovery;

import io.vertx.codegen.annotations.VertxGen;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
public interface Service {

  <T> T get();

  void release();

  //TODO health handler.
}
