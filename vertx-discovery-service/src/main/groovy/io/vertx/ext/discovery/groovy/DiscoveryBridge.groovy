/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.ext.discovery.groovy;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import io.vertx.groovy.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
*/
@CompileStatic
public class DiscoveryBridge {
  private final def io.vertx.ext.discovery.DiscoveryBridge delegate;
  public DiscoveryBridge(Object delegate) {
    this.delegate = (io.vertx.ext.discovery.DiscoveryBridge) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void start(Vertx vertx, DiscoveryService discovery, Map<String, Object> configuration, Handler<AsyncResult<Void>> completionHandler) {
    this.delegate.start((io.vertx.core.Vertx)vertx.getDelegate(), (io.vertx.ext.discovery.DiscoveryService)discovery.getDelegate(), configuration != null ? new io.vertx.core.json.JsonObject(configuration) : null, completionHandler);
  }
  public void stop(Vertx vertx, DiscoveryService discovery) {
    this.delegate.stop((io.vertx.core.Vertx)vertx.getDelegate(), (io.vertx.ext.discovery.DiscoveryService)discovery.getDelegate());
  }
}
