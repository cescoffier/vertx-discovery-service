package io.vertx.ext.discovery;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
public class Record {

  public static final String ENDPOINT = "endpoint";

  private JsonObject location;

  private JsonObject metadata = new JsonObject();

  private String name;

  private Status status = Status.UNKNOWN;

  private String registration;

  private String type;

  public Record() {

  }

  public Record(JsonObject json) {
    this();
    RecordConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    RecordConverter.toJson(this, json);
    return json;
  }

  public Record(Record other) {
    this.location = other.location;
    this.metadata = other.metadata;
    this.name = other.name;
    this.status = other.status;
    this.registration = other.registration;
    this.type = other.type;
  }

  public JsonObject getLocation() {
    return location;
  }

  public Record setLocation(JsonObject location) {
    this.location = location;
    return this;
  }

  public JsonObject getMetadata() {
    return metadata;
  }

  public Record setMetadata(JsonObject metadata) {
    this.metadata = metadata;
    return this;
  }

  public String getName() {
    return name;
  }

  public Record setName(String name) {
    this.name = name;
    return this;
  }

  public Status getStatus() {
    return status;
  }

  public Record setStatus(Status status) {
    this.status = status;
    return this;
  }

  public Record setRegistration(String serviceId) {
    this.registration = serviceId;
    return this;
  }

  public String getRegistration() {
    return registration;
  }

  public String getType() {
    return type;
  }

  public Record setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * Checks whether or not the current {@link Record} matches the filter.
   *
   * @param filter the filter
   * @return whether or not the record matches the filter
   */
  public boolean match(JsonObject filter) {
    for (String key : filter.fieldNames()) {
      boolean match;
      switch (key) {
        case "name":
          match = match(getName(), filter.getString("name"));
          break;
        case "registration":
          match = match(getRegistration(), filter.getString("registration"));
          break;
        case "status":
          match = match(getStatus().name(), filter.getString("status"));
          break;
        default:
          // metadata
          match = match(getMetadata().getValue(key), filter.getValue(key));
          break;
      }

      if (!match) {
        return false;
      }
    }

    return true;
  }

  private boolean match(Object actual, Object expected) {
    return actual != null
        && ("*".equals(expected) ||
        (actual instanceof String ?
            ((String) actual).equalsIgnoreCase(expected.toString()) : actual.equals(expected)));
  }

}
