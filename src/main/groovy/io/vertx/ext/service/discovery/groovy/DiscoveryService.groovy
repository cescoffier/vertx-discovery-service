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

package io.vertx.ext.service.discovery.groovy;
import groovy.transform.CompileStatic
import io.vertx.lang.groovy.InternalHelper
import io.vertx.core.json.JsonObject
import java.util.List
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
*/
@CompileStatic
public class DiscoveryService {
  private final def io.vertx.ext.service.discovery.DiscoveryService delegate;
  public DiscoveryService(Object delegate) {
    this.delegate = (io.vertx.ext.service.discovery.DiscoveryService) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public void publish(String serviceItf, Map<String, Object> properties, String address, Handler<AsyncResult<String>> resultHandler) {
    this.delegate.publish(serviceItf, properties != null ? new io.vertx.core.json.JsonObject(properties) : null, address, resultHandler);
  }
  public void unpublish(String id, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.unpublish(id, resultHandler);
  }
  public void getService(String serviceItf, Map<String, Object> filter, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.getService(serviceItf, filter != null ? new io.vertx.core.json.JsonObject(filter) : null, new Handler<AsyncResult<io.vertx.core.json.JsonObject>>() {
      public void handle(AsyncResult<io.vertx.core.json.JsonObject> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void getServices(String serviceItf, Map<String, Object> filter, Handler<AsyncResult<List<Map<String, Object>>>> resultHandler) {
    this.delegate.getServices(serviceItf, filter != null ? new io.vertx.core.json.JsonObject(filter) : null, new Handler<AsyncResult<List<JsonObject>>>() {
      public void handle(AsyncResult<List<JsonObject>> event) {
        AsyncResult<List<Map<String, Object>>> f
        if (event.succeeded()) {
          f = InternalHelper.<List<Map<String, Object>>>result(event.result().collect({
            io.vertx.core.json.JsonObject element ->
            InternalHelper.wrapObject(element)
          }) as List)
        } else {
          f = InternalHelper.<List<Map<String, Object>>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
}
