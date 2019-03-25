package com.simplify.threedpoc;


import com.simplify.payments.PaymentsApi;
import com.simplify.payments.PaymentsMap;
import com.simplify.payments.domain.CardToken;
import com.simplify.payments.domain.Payment;
import com.simplify.payments.exception.ApiCommunicationException;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.json.simple.JSONObject;

import java.util.UUID;

public class PaymentProcessorVerticle extends AbstractVerticle {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void start(Future<Void> future) {
        if (config().getString("threeds.sandbox.url") != null) {
            PaymentsApi.API_BASE_SANDBOX_URL = config().getString("threeds.sandbox.url");
        }

        if (config().getString("threeds.live.url") != null) {
            PaymentsApi.API_BASE_LIVE_URL = config().getString("threeds.live.url");
        }

        log.info("Public key: " + config().getString("threeds.public_key") + " - Endpoint: " + PaymentsApi.API_BASE_LIVE_URL);
        PaymentsApi.PUBLIC_KEY = config().getString("threeds.public_key");
        PaymentsApi.PRIVATE_KEY = config().getString("threeds.private_key");

        EventBus eb = vertx.eventBus();
        eb.<JsonObject>consumer("threeds.payment.create", message -> {

            log.info("Processing payment...");
            JsonObject body = message.body();
            JsonObject response = new JsonObject();

            String cardNumber = body.getString("card.number");
            String cvc = body.getString("card.cvc");
            String expiryMonth = body.getString("card.expiryMonth");
            String expiryYear = body.getString("card.expiryYear");
            String currency = body.getString("currency");
            long amount = body.getLong("amount");

            try {

                CardToken cardToken = CardToken.create(new PaymentsMap()
                        .set("card.addressCity", "OFallon")
                        .set("card.addressState", "MO")
                        .set("card.cvc", cvc)
                        .set("card.expMonth", expiryMonth)
                        .set("card.expYear", expiryYear)
                        .set("card.number", cardNumber)
                        .set("secure3DRequestData.amount", amount)
                        .set("secure3DRequestData.currency", currency)
                        .set("secure3DRequestData.description", UUID.randomUUID().toString())
                );

                String token = cardToken.get("id").toString();
                log.info("Token = " + token);

                // Note that this is not Vert.x JsonObject here.
                JSONObject card = (JSONObject) cardToken.get("card.secure3DData");

                if (card != null) {

                    JsonObject threeSecureJson = new JsonObject();

                    threeSecureJson.put("enrolled", card.get("isEnrolled"));
                    threeSecureJson.put("acsUrl", card.get("acsUrl"));
                    threeSecureJson.put("termUrl", card.get("termUrl"));
                    threeSecureJson.put("paReq", card.get("paReq"));
                    threeSecureJson.put("processorReference", card.get("processorReference"));
                    threeSecureJson.put("md", card.get("md"));

                    response.put("3dsecure", threeSecureJson);

                }

                response.put("token", token);
                response.put("success", true);
                response.put("currency",currency);

                message.reply(response);

            } catch (Exception e) {
                log.error("Error processing card token", e);

                JsonObject errorMessage = new JsonObject();
                errorMessage.put("success", false);
                errorMessage.put("message", e.getMessage());
                message.fail(500, errorMessage.toString());
            }
        });

        eb.<JsonObject>consumer("threeds.payment.complete", message -> {

            log.info("Completing payment...");
            JsonObject body = message.body();

            try {
                Payment payment = Payment.create(new PaymentsMap()
                        .set("amount", 1500)
                        .set("currency", body.getString("currency"))
                        .set("description", "description")
                        .set("token", body.getString("token"))
                );

                JsonObject response = new JsonObject();
                log.info("Payment status: " + payment.get("paymentStatus"));
                if ("APPROVED".equals(payment.get("paymentStatus"))) {
                    response.put("success", true);
                } else {
                    response.put("success", false);
                }

                message.reply(response);

            } catch (Exception e) {
                log.error("Error processing payment", e);

                JsonObject errorMessage = new JsonObject();
                errorMessage.put("success", false);
                errorMessage.put("message", e.getMessage());
                message.fail(500, errorMessage.toString());
            }
        });
    }
}
