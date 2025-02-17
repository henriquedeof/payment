package com.xpto.distancelearning.payment.consumers;

import com.xpto.distancelearning.payment.dtos.PaymentCommandDto;
import com.xpto.distancelearning.payment.services.PaymentService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    @Autowired
    private PaymentService paymentService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${dl.broker.queue.paymentCommandQueue.name}", durable = "true"),
            exchange = @Exchange(value = "${dl.broker.exchange.paymentCommandExchange}", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = "${dl.broker.key.paymentCommandKey}") // henrique: I need to set a Key as this is a TOPIC exchange
    )
    public void listenPaymentCommand(@Payload PaymentCommandDto paymentCommandDto) {
        paymentService.makePayment(paymentCommandDto);
    }
}