package com.xpto.distancelearning.payment.services;

import com.xpto.distancelearning.payment.models.CreditCardModel;
import com.xpto.distancelearning.payment.models.PaymentModel;

public interface PaymentStripeService {

    PaymentModel processStripePayment(PaymentModel paymentModel, CreditCardModel creditCardModel);
}