package io.vertx.ext.discovery.types;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
class HttpLocation {


  private String host;
  private int port;
  private String root;
  private String endpoint;

  public HttpLocation() {

  }

  public HttpLocation(HttpLocation other) {
    this.host = other.host;
    this.port = other.port;
    this.root = other.root;
  }

  public HttpLocation(JsonObject json) {
    this();
    HttpLocationConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    HttpLocationConverter.toJson(this, json);
    return json;
  }

  public String getHost() {
    return host;
  }

  public HttpLocation setHost(String host) {
    this.host = host;
    setEndpoint("http://" + host + ":" + port + root);
    return this;
  }

  public HttpLocation setEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public int getPort() {
    return port;
  }

  public HttpLocation setPort(int port) {
    this.port = port;
    setEndpoint("http://" + host + ":" + port + root);
    return this;
  }

  public String getRoot() {
    setEndpoint("http://" + host + ":" + port + root);
    return root;
  }

  public HttpLocation setRoot(String root) {
    if (root.startsWith("/")) {
      this.root = root;
    } else {
      this.root = "/" + root;
    }
    setEndpoint("http://" + host + ":" + port + root);
    return this;
  }
}
