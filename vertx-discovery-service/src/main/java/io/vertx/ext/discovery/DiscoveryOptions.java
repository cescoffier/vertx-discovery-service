package io.vertx.ext.discovery;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
public class DiscoveryOptions {

  public static final String DEFAULT_DISCOVERY_ADDRESS = "vertx.discovery";
  private static final boolean DEFAULT_EXPOSED_AS_SERVICE = false;
  public static final String DEFAULT_ANNOUNCE_ADDRESS = "vertx.discovery.announce";
  private String address = DEFAULT_DISCOVERY_ADDRESS;
  private boolean exposedAsService = DEFAULT_EXPOSED_AS_SERVICE;
  private String announceAddress = DEFAULT_ANNOUNCE_ADDRESS;

  public DiscoveryOptions() {

  }

  public DiscoveryOptions(DiscoveryOptions other) {
    this.address = other.address;
    this.exposedAsService = other.exposedAsService;
    this.announceAddress = other.announceAddress;
  }

  public DiscoveryOptions(JsonObject json) {
    this();
    DiscoveryOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    DiscoveryOptionsConverter.toJson(this, json);
    return json;
  }

  public String getAddress() {
    return address;
  }

  public DiscoveryOptions setAddress(String address) {
    this.address = address;
    return this;
  }

  public boolean isExposedAsService() {
    return exposedAsService;
  }

  public DiscoveryOptions setExposedAsService(boolean exposedAsService) {
    this.exposedAsService = exposedAsService;
    return this;
  }

  public String getAnnounceAddress() {
    return announceAddress;
  }

  public DiscoveryOptions setAnnounceAddress(String announceAddress) {
    this.announceAddress = announceAddress;
    return this;
  }

}
