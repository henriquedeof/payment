package com.xpto.distancelearning.payment.publishers;

import com.xpto.distancelearning.payment.dtos.PaymentCommandDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentCommandPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value(value = "${dl.broker.exchange.paymentCommandExchange}")
    private String paymentCommandExchange;

    @Value(value = "${dl.broker.key.paymentCommandKey}")
    private String paymentCommandKey;

    public void publishPaymentCommand(PaymentCommandDto paymentCommandDto) {
        rabbitTemplate.convertAndSend(paymentCommandExchange, paymentCommandKey, paymentCommandDto);
    }

}