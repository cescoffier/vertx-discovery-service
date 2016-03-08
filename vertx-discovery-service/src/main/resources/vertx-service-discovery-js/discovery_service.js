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

/** @module vertx-service-discovery-js/discovery_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');
var Service = require('vertx-service-discovery-js/service');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JDiscoveryService = io.vertx.ext.discovery.DiscoveryService;
var DiscoveryOptions = io.vertx.ext.discovery.DiscoveryOptions;
var Record = io.vertx.ext.discovery.Record;

/**

 @class
*/
var DiscoveryService = function(j_val) {

  var j_discoveryService = j_val;
  var that = this;

  /**

   @public

   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_discoveryService["close()"]();
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param record {Object} 
   @param resultHandler {function} 
   */
  this.publish = function(record, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_discoveryService["publish(io.vertx.ext.discovery.Record,io.vertx.core.Handler)"](record != null ? new Record(new JsonObject(JSON.stringify(record))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param id {string} 
   @param resultHandler {function} 
   */
  this.unpublish = function(id, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_discoveryService["unpublish(java.lang.String,io.vertx.core.Handler)"](id, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param filter {Object} 
   @param resultHandler {function} 
   */
  this.getRecord = function(filter, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_discoveryService["getRecord(io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](utils.convParamJsonObject(filter), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param filter {Object} 
   @param resultHandler {function} 
   */
  this.getRecords = function(filter, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_discoveryService["getRecords(io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](utils.convParamJsonObject(filter), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnListSetDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param record {Object} 
   @param resultHandler {function} 
   */
  this.update = function(record, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && (typeof __args[0] === 'object' && __args[0] != null) && typeof __args[1] === 'function') {
      j_discoveryService["update(io.vertx.ext.discovery.Record,io.vertx.core.Handler)"](record != null ? new Record(new JsonObject(JSON.stringify(record))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnDataObject(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_discoveryService;
};

/**

 @memberof module:vertx-service-discovery-js/discovery_service
 @param vertx {Vertx} 
 @param options {Object} 
 @return {DiscoveryService}
 */
DiscoveryService.create = function(vertx, options) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(JDiscoveryService["create(io.vertx.core.Vertx,io.vertx.ext.discovery.DiscoveryOptions)"](vertx._jdel, options != null ? new DiscoveryOptions(new JsonObject(JSON.stringify(options))) : null), DiscoveryService);
  } else throw new TypeError('function invoked with invalid arguments');
};

/**

 @memberof module:vertx-service-discovery-js/discovery_service
 @param vertx {Vertx} 
 @param record {Object} 
 @return {Service}
 */
DiscoveryService.getService = function(vertx, record) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(JDiscoveryService["getService(io.vertx.core.Vertx,io.vertx.ext.discovery.Record)"](vertx._jdel, record != null ? new Record(new JsonObject(JSON.stringify(record))) : null), Service);
  } else throw new TypeError('function invoked with invalid arguments');
};

// We export the Constructor function
module.exports = DiscoveryService;