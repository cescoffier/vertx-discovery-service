/**
 * == Vert.x Circuit Breaker
 *
 * Vert.x Circuit Breaker is an implementation of the Circuit Breaker _pattern_ for Vert.x. It keeps tracks the
 * number of failures and _open the circuit_ when a threshold is reached. Optionally, a fallback is executed.
 *
 * Supported failures are:
 *
 * * failures reported by your code in a {@link io.vertx.core.Future}
 * * exception thrown by your code
 * * uncompleted futures (timeout)
 *
 * == Using vertx circuit breaker
 *
 * To use the Vert.x Circuit Breaker, add the following dependency to the _dependencies_ section of your build
 * descriptor:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 *   <groupId>${maven.groupId}</groupId>
 *   <artifactId>${maven.artifactId}</artifactId>
 *   <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * ----
 *
 * == Using the circuit breaker
 *
 * To use the circuit breaker you need to:
 *
 * 1. Create a circuit breaker, with the configuration you want (timeout, number of failure before opening the circuit)
 * 2. Execute some code using the breaker
 *
 * Here is an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example1(io.vertx.core.Vertx)}
 * ----
 *
 * Your code can take a {@link io.vertx.core.Future} as parameter when the completion is asynchronous:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example2(io.vertx.core.Vertx)}
 * ----
 *
 * Optionally, you can provide a fallback executed when the circuit is open:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example3(io.vertx.core.Vertx)}
 * ----
 *
 * The fallback can also be set on the {@link io.vertx.ext.circuitbreaker.CircuitBreaker} object directly:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example4(io.vertx.core.Vertx)}
 * ----
 *
 * == Callbacks
 *
 * You can also configures callbacks invoked when the circuit is opened or closed:
 *
 * [source,$lang]
 * ----
 * {@link examples.Examples#example5(io.vertx.core.Vertx)}
 * ----
 *
 * You can also be notified when the circuit breaker decide to attempt to reset (half-open state). You can register
 * such as callback with {@link io.vertx.ext.circuitbreaker.CircuitBreaker#halfOpenHandler(io.vertx.core.Handler)}.
 *
 * == Event bus notification
 *
 * Every time the circuit state changes, an event is published on the event bus to `circuit-breaker[name]`, where
 * `name` is replaced with the name passed when creating the {@link io.vertx.ext.circuitbreaker.CircuitBreaker}. Each
 * event contains a Json Object with:
 *
 * * `state` : the new circuit breaker state (`OPEN`, `CLOSED`, `HALF_OPEN`)
 * * `name` : the name of the circuit breaker
 * * `failures` : the number of failures
 * * `node` : the identifier of the node (`local` is Vert.x is not running in cluster mode)
 *
 * == The half-open state
 *
 * When the circuit is “open,” calls to the circuit breaker fail immediately, without any attempt to execute the real
 * operation. After a suitable amount of time (configured from
 * {@link io.vertx.ext.circuitbreaker.CircuitBreakerOptions#setResetTimeoutInMs(long)}, the circuit breaker decides that the
 * operation has a chance of succeeding, so it goes into the {@code half-open} state. In this state, the next call to the
 * circuit breaker is allowed to execute the dangerous operation. Should the call succeed, the circuit breaker resets
 * and returns to the {@code closed} state, ready for more routine operation. If this trial call fails, however, the circuit
 * breaker returns to the {@code open} state until another timeout elapses.
 */
@ModuleGen(name = "vertx-circuit-breaker", groupPackage = "io.vertx.ext.circuitbreaker")
@Document(fileName = "index.ad")
package io.vertx.ext.circuitbreaker;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;