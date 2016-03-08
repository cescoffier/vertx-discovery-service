package io.vertx.ext.discovery.spi;

import io.vertx.core.Vertx;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.Service;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface ServiceType {

  String name();

  Service get(Vertx vertx, Record record);

}
