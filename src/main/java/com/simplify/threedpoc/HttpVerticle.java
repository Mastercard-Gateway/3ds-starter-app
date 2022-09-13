package com.simplify.threedpoc;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;


public class HttpVerticle extends AbstractVerticle {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start() {

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(StaticHandler.create());

        router.post("/pay").handler(this::processCardToken);
        router.post("/complete").handler(this::processPayment);

        router.post("/payEmvCreate").handler(this::processCardTokenEmvCreate);
        router.post("/payEmvUpdate").handler(this::processCardTokenEmvUpdate);


        server.requestHandler(router::accept)
                .listen(config()
                        .getInteger("threeds.server.port", 8085));
    }

    private void processCardToken(RoutingContext ctx) {
        EventBus eventBus = vertx.eventBus();

        JsonObject message = constructJsonObject(ctx);

        eventBus.<JsonObject>send("threeds.payment.create", message, event -> {

            if (event.succeeded()) {
                JsonObject response = event.result().body();

                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(response.encodePrettily());
            } else {
                ctx.fail(503);
            }
        });
    }

    private void processPayment(RoutingContext ctx) {
        EventBus eventBus = vertx.eventBus();

        String currency = ctx.request().getParam("currency");
        String token = ctx.request().getParam("token");

        JsonObject message = new JsonObject();
        message.put("token", token);
        message.put("currency",currency);

        eventBus.<JsonObject>send("threeds.payment.complete", message, event -> {

            if (event.succeeded()) {
                JsonObject response = event.result().body();

                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(response.encodePrettily());
            } else {
                ctx.fail(503);
            }
        });

    }

    private void processCardTokenEmvCreate(RoutingContext ctx) {
        EventBus eventBus = vertx.eventBus();

        JsonObject message = constructJsonObject(ctx);

        eventBus.<JsonObject>send("threeds.payment.emv.create", message, event -> {

            if (event.succeeded()) {
                JsonObject response = event.result().body();

                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(response.encodePrettily());
            } else {
                ctx.fail(503);
            }
        });
    }

    private void processCardTokenEmvUpdate(RoutingContext ctx) {
        EventBus eventBus = vertx.eventBus();

        String token = ctx.request().getParam("token");
        String currency = ctx.request().getParam("currency");
        String browser = ctx.request().getParam("browser");
        String timezone = ctx.request().getParam("timezone");

        JsonObject message = new JsonObject();
        message.put("token", token);
        message.put("currency", currency);
        message.put("browser", browser);
        message.put("timezone", timezone);

        eventBus.<JsonObject>send("threeds.payment.emv.update", message, event -> {

            if (event.succeeded()) {
                JsonObject response = event.result().body();

                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(response.encodePrettily());
            } else {
                ctx.fail(503);
            }
        });
    }

    private JsonObject constructJsonObject(RoutingContext ctx){
        String cardNumber = ctx.request().getParam("cc_number");
        String cardExpiryYear = ctx.request().getParam("cc_exp_year");
        String cardExpiryMonth = ctx.request().getParam("cc_exp_month");
        String cardCvc = ctx.request().getParam("cc_cvc");
        String currency = ctx.request().getParam("currency");
        String amountString = ctx.request().getParam("amount");
        long amount = Long.valueOf(amountString);

        JsonObject message = new JsonObject();
        message.put("card.number", cardNumber);
        message.put("card.expiryYear", cardExpiryYear);
        message.put("card.expiryMonth", cardExpiryMonth);
        message.put("card.cvc", cardCvc);
        message.put("currency",currency);
        message.put("amount",amount);
        return message;
    }
}
