package com.xpto.distancelearning.payment.services.impl;

import com.stripe.Stripe;
import com.stripe.exception.CardException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.xpto.distancelearning.payment.enums.PaymentControl;
import com.xpto.distancelearning.payment.models.CreditCardModel;
import com.xpto.distancelearning.payment.models.PaymentModel;
import com.xpto.distancelearning.payment.services.PaymentStripeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentStripeServiceImpl implements PaymentStripeService {
    private static final Logger logger = LogManager.getLogger(PaymentStripeServiceImpl.class);

    @Value(value = "${dl.stripe.secretKey}")
    private String secretKeyStripe;

    @Override
    public PaymentModel processStripePayment(PaymentModel paymentModel, CreditCardModel creditCardModel) {
        Stripe.apiKey = secretKeyStripe;
        String paymentIntentId = null;

        try {
            // Step 1: Create a PaymentIntent
            List<Object> paymentMethodTypes = new ArrayList<>();
            paymentMethodTypes.add("card");
            Map<String, Object> paramsPaymentIntent = new HashMap<>();
            paramsPaymentIntent.put("amount", paymentModel.getValuePaid().multiply(new BigDecimal("100")).longValue());
            paramsPaymentIntent.put("currency", "aud");
            paramsPaymentIntent.put("payment_method_types", paymentMethodTypes);
            PaymentIntent paymentIntent = PaymentIntent.create(paramsPaymentIntent);
            paymentIntentId = paymentIntent.getId();

            // Step 2: Create payment method
            Map<String, Object> card = new HashMap<>();
            card.put("number", creditCardModel.getCreditCardNumber().replaceAll(" ", ""));
            card.put("exp_month", creditCardModel.getExpirationDate().split("/")[0]);
            card.put("exp_year", creditCardModel.getExpirationDate().split("/")[1]);
            card.put("cvc", creditCardModel.getCvvCode());
            Map<String, Object> paramsPaymentMethod = new HashMap<>();
            paramsPaymentMethod.put("type", "card");
            paramsPaymentMethod.put("card", card);
            PaymentMethod paymentMethod = PaymentMethod.create(paramsPaymentMethod);

            // Step 3: Confirm the PaymentIntent
            Map<String, Object> paramsPaymentConfirm = new HashMap<>();
            paramsPaymentConfirm.put("payment_method", paymentMethod.getId());
            PaymentIntent confirmPaymentIntent = paymentIntent.confirm(paramsPaymentConfirm);

            if (confirmPaymentIntent.getStatus().equals("succeeded")) {
                paymentModel.setPaymentControl(PaymentControl.EFFECTED);
                paymentModel.setPaymentMessage("payment effected - paymentIntent: " + paymentIntentId);
                paymentModel.setPaymentCompletionDate(LocalDateTime.now(ZoneId.of("UTC")));
            } else {
                paymentModel.setPaymentControl(PaymentControl.ERROR);
                paymentModel.setPaymentMessage("payment error v1 - paymentIntent: " + paymentIntentId);
            }

        } catch (CardException cardException) {
            logger.error("A payment error occurred", cardException);
            try {
                paymentModel.setPaymentControl(PaymentControl.REFUSED);
                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                paymentModel.setPaymentMessage("payment refused v1 - paymentIntent: " + paymentIntentId +
                        ", cause: " + paymentIntent.getLastPaymentError().getCode() +
                        ", message: " + paymentIntent.getLastPaymentError().getMessage());
            } catch (Exception exception) {
                paymentModel.setPaymentMessage("payment refused v2 - paymentIntent: " + paymentIntentId);
                logger.error("Another problem occurred, maybe unrelated to Stripe.", exception);
            }
        } catch (Exception exception) {
            paymentModel.setPaymentControl(PaymentControl.ERROR);
            paymentModel.setPaymentMessage("payment error v2 - paymentIntent: " + paymentIntentId);
            logger.error("Another problem occurred, maybe unrelated to Stripe.", exception);
        }
        return paymentModel;
    }
}