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
import java.util.List
import io.vertx.ext.discovery.DiscoveryOptions
import io.vertx.groovy.core.Vertx
import io.vertx.ext.discovery.Record
import io.vertx.core.json.JsonObject
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
*/
@CompileStatic
public class DiscoveryService {
  private final def io.vertx.ext.discovery.DiscoveryService delegate;
  public DiscoveryService(Object delegate) {
    this.delegate = (io.vertx.ext.discovery.DiscoveryService) delegate;
  }
  public Object getDelegate() {
    return delegate;
  }
  public static DiscoveryService create(Vertx vertx, Map<String, Object> options) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.discovery.DiscoveryService.create((io.vertx.core.Vertx)vertx.getDelegate(), options != null ? new io.vertx.ext.discovery.DiscoveryOptions(new io.vertx.core.json.JsonObject(options)) : null), io.vertx.ext.discovery.groovy.DiscoveryService.class);
    return ret;
  }
  public static Service getService(Vertx vertx, Map<String, Object> record) {
    def ret= InternalHelper.safeCreate(io.vertx.ext.discovery.DiscoveryService.getService((io.vertx.core.Vertx)vertx.getDelegate(), record != null ? new io.vertx.ext.discovery.Record(new io.vertx.core.json.JsonObject(record)) : null), io.vertx.ext.discovery.groovy.Service.class);
    return ret;
  }
  public void close() {
    this.delegate.close();
  }
  public void publish(Map<String, Object> record = [:], Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.publish(record != null ? new io.vertx.ext.discovery.Record(new io.vertx.core.json.JsonObject(record)) : null, new Handler<AsyncResult<io.vertx.ext.discovery.Record>>() {
      public void handle(AsyncResult<io.vertx.ext.discovery.Record> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()?.toJson()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void unpublish(String id, Handler<AsyncResult<Void>> resultHandler) {
    this.delegate.unpublish(id, resultHandler);
  }
  public void getRecord(Map<String, Object> filter, Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.getRecord(filter != null ? new io.vertx.core.json.JsonObject(filter) : null, new Handler<AsyncResult<io.vertx.ext.discovery.Record>>() {
      public void handle(AsyncResult<io.vertx.ext.discovery.Record> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()?.toJson()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void getRecords(Map<String, Object> filter, Handler<AsyncResult<List<Map<String, Object>>>> resultHandler) {
    this.delegate.getRecords(filter != null ? new io.vertx.core.json.JsonObject(filter) : null, new Handler<AsyncResult<List<Record>>>() {
      public void handle(AsyncResult<List<Record>> event) {
        AsyncResult<List<Map<String, Object>>> f
        if (event.succeeded()) {
          f = InternalHelper.<List<Map<String, Object>>>result(event.result().collect({
            io.vertx.ext.discovery.Record element ->
            (Map<String, Object>)InternalHelper.wrapObject(element?.toJson())
          }) as List)
        } else {
          f = InternalHelper.<List<Map<String, Object>>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
  public void update(Map<String, Object> record = [:], Handler<AsyncResult<Map<String, Object>>> resultHandler) {
    this.delegate.update(record != null ? new io.vertx.ext.discovery.Record(new io.vertx.core.json.JsonObject(record)) : null, new Handler<AsyncResult<io.vertx.ext.discovery.Record>>() {
      public void handle(AsyncResult<io.vertx.ext.discovery.Record> event) {
        AsyncResult<Map<String, Object>> f
        if (event.succeeded()) {
          f = InternalHelper.<Map<String, Object>>result((Map<String, Object>)InternalHelper.wrapObject(event.result()?.toJson()))
        } else {
          f = InternalHelper.<Map<String, Object>>failure(event.cause())
        }
        resultHandler.handle(f)
      }
    });
  }
}
