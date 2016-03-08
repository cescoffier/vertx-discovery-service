package io.vertx.ext.msa.abcd.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@VertxGen
@ProxyGen
public interface HelloService {

  void say(String name, Handler<AsyncResult<String>> resultHandler);

}
