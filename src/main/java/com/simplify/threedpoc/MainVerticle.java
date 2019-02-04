package com.simplify.threedpoc;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;


/**
 * The main verticle retrieves the configuration from the config file and system
 * variables, and deploys the different Verticles of the application.
 */
public class MainVerticle extends AbstractVerticle {

    public void start() {

        ConfigRetriever retriever = getConfigRetriever();

        retriever.getConfig(json -> {

            JsonObject config = json.result();
            if (json.succeeded()) {
                vertx.deployVerticle(HttpVerticle.class, new DeploymentOptions()
                        .setConfig(config));
                vertx.deployVerticle(PaymentProcessorVerticle.class, new DeploymentOptions()
                        .setConfig(config)
                        .setWorkerPoolName("payment-pool")
                        .setWorkerPoolSize(5)
                        .setInstances(5)
                        .setWorker(true));
            }

        });
    }

    private ConfigRetriever getConfigRetriever() {
        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setConfig(new JsonObject().put("path", "conf/config.json"));

        ConfigStoreOptions sysPropsStore = new ConfigStoreOptions().setType("sys");

        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(fileStore)
                .addStore(sysPropsStore);

        return ConfigRetriever.create(vertx, options);
    }
}
