package io.vertx.ext.circuitbreaker;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public enum CircuitBreakerState {
  OPEN,
  CLOSED,
  HALF_OPEN
}
