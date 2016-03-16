package io.vertx.ext.discovery.bridge.kubernates;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceList;
import io.fabric8.kubernetes.api.model.ServicePort;
import io.fabric8.kubernetes.client.*;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.discovery.DiscoveryBridge;
import io.vertx.ext.discovery.DiscoveryService;
import io.vertx.ext.discovery.Record;
import io.vertx.ext.discovery.spi.ServiceType;
import io.vertx.ext.discovery.types.HttpLocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static io.vertx.ext.discovery.types.HttpEndpoint.TYPE;

/**
 * A discovery bridge listening for kubernates services and publishing them in the Vert.x discovery. This bridge only
 * support the importation of services from kubernates in vert.x (and not the opposite).
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class KubernatesBridge implements DiscoveryBridge, Watcher<Service> {

  private KubernetesClient client;
  private DiscoveryService discovery;

  private final static Logger LOGGER = LoggerFactory.getLogger(KubernatesBridge.class.getName());
  private String namespace;
  private List<Record> records = new CopyOnWriteArrayList<>();
  private Watch watcher;

  @Override
  public void start(Vertx vertx, DiscoveryService discovery, JsonObject configuration, Handler<AsyncResult<Void>>
      completionHandler) {
    this.discovery = discovery;

    JsonObject configuration1;
    if (configuration == null) {
      configuration1 = new JsonObject();
    } else {
      configuration1 = configuration;
    }

    // 1) get kubernates auth info
    this.namespace = configuration1.getString("namespace", "default");
    LOGGER.info("Kubernates discovery configured for namespace: " + namespace);

    vertx.<KubernetesClient>executeBlocking(
        future -> {
          String accountToken = getAccountToken();
          LOGGER.info("Kubernetes Discovery: Bearer Token { " + accountToken + " }");
          Config config = new ConfigBuilder().withOauthToken(accountToken).build();
          DefaultKubernetesClient kubernatesClient = null;
          try {
            kubernatesClient = new DefaultKubernetesClient(config);
            ServiceList list = kubernatesClient.services().inNamespace(namespace).list();
            synchronized (KubernatesBridge.this) {
              watcher = kubernatesClient.services().inNamespace(namespace)
                  .watch(KubernatesBridge.this);
              for (Service service : list.getItems()) {
                Record record = createRecord(service);
                if (addRecordIfNotContained(record)) {
                  publishRecord(record);
                }
              }
            }
            future.complete(kubernatesClient);
          } catch (KubernetesClientException e) {
            if (kubernatesClient != null) {
              kubernatesClient.close();
            }
            future.fail(e);
          }
        },
        ar -> {
          if (ar.succeeded()) {
            this.client = ar.result();
            LOGGER.info("Kubernates client instantiated");
          } else {
            LOGGER.error("Error while interacting with kubernates", ar.cause());
          }
        }
    );
  }

  private void publishRecord(Record record) {
    discovery.publish(record, ar -> {
      if (ar.succeeded()) {
        LOGGER.info("Kubernates service published in the vert.x service registry :", record);
      } else {
        LOGGER.error("Kubernates service not published in the vert.x service registry",
            ar.cause());
      }
    });
  }

  private synchronized boolean addRecordIfNotContained(Record record) {
    for (Record rec : records) {
      if (areTheSameService(rec, record)) {
        return false;
      }
    }
    return records.add(record);
  }

  private boolean areTheSameService(Record record1, Record record2) {
    return
        record1.getMetadata().equals(record2.getMetadata());
  }

  private Record createRecord(Service service) {
    Record record = new Record();
    record.setName(service.getMetadata().getName());
    for (Map.Entry<String, String> entry : service.getMetadata().getLabels().entrySet()) {
      record.getMetadata().put(entry.getKey(), entry.getValue());
    }
    record.getMetadata().put("kubernates.namespace", service.getMetadata().getNamespace());
    record.getMetadata().put("kubernates.name", service.getMetadata().getName());
    record.getMetadata().put("kubernates.uuid", service.getMetadata().getUid());

    // Compute type
    List<ServicePort> ports = service.getSpec().getPorts();
    JsonObject defaultLocation = null;
    for (ServicePort port : ports) {
      if (defaultLocation == null) {
        defaultLocation = new JsonObject();
        if (port.getTargetPort().getIntVal() != null) {
          defaultLocation.put("port", port.getTargetPort().getIntVal());
        }
        defaultLocation.put("internal-port", port.getPort());
        defaultLocation.put("name", port.getName());
        defaultLocation.put("protocol", port.getProtocol());

      }
      // Right now we support only HTTP services
      if (isHttp(port)) {
        record.setType(TYPE)
            .setLocation(new HttpLocation()
                .setHost(service.getSpec().getClusterIP())
                .setPort(port.getTargetPort().getIntVal()).toJson());
        return record;
      }

      //TODO extends with some well-known data sources (mysql, postgres, mongo, redis)


    }

    if (record.getType() == null) {
      record.setType(ServiceType.UNKNOWN);
      if (defaultLocation != null) {
        record.setLocation(defaultLocation);
      }
    }
    return record;
  }

  private boolean isHttp(ServicePort port) {
    return port.getPort() == 80
        || port.getPort() >= 8080 && port.getPort() < 9010
        || port.getPort() == 433;
  }

  @Override
  public void stop(Vertx vertx, DiscoveryService discovery) {
    synchronized (this) {
      if (watcher != null) {
        watcher.close();
        watcher = null;
      }

      if (client != null) {
        client.close();
        client = null;
      }
    }
  }

  private String getAccountToken() {
    try {
      String tokenFile = "/var/run/secrets/kubernetes.io/serviceaccount/token";
      File file = new File(tokenFile);
      byte[] data = new byte[(int) file.length()];
      InputStream is = new FileInputStream(file);
      is.read(data);
      return new String(data);

    } catch (IOException e) {
      throw new RuntimeException("Could not get token file", e);
    }
  }

  @Override
  public synchronized void eventReceived(Action action, Service service) {
    switch (action) {
      case ADDED:
        // new service
        Record record = createRecord(service);
        if (addRecordIfNotContained(record)) {
          publishRecord(record);
        }
        break;
      case DELETED:
      case ERROR:
        // remove service
        record = createRecord(service);
        Record storedRecord = removeRecordIfContained(record);
        if (storedRecord != null) {
          unpublishRecord(storedRecord);
        }
        break;
      case MODIFIED:
        record = createRecord(service);
        storedRecord = removeRecordIfContained(record);
        if (storedRecord != null) {
          publishRecord(record);
        }
    }

  }

  private void unpublishRecord(Record record) {
    discovery.unpublish(record.getRegistration(), ar -> {
      if (ar.failed()) {
        LOGGER.error("Cannot unregister kubernates service", ar.cause());
      } else {
        LOGGER.info("Kubernates service unregistered from the vert.x registry");
      }
    });
  }

  private Record removeRecordIfContained(Record record) {
    for (Record rec : records) {
      if (areTheSameService(rec, record)) {
        records.remove(rec);
        return rec;
      }
    }
    return null;
  }

  @Override
  public void onClose(KubernetesClientException e) {
    // rather bad, un-publish all the services
    records.forEach(this::unpublishRecord);
  }
}
