package com.xpto.distancelearning.payment.services;

import com.xpto.distancelearning.payment.dtos.PaymentCommandDto;
import com.xpto.distancelearning.payment.dtos.PaymentRequestDto;
import com.xpto.distancelearning.payment.models.PaymentModel;
import com.xpto.distancelearning.payment.models.UserModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;
import java.util.UUID;

public interface PaymentService {
    PaymentModel requestPayment(PaymentRequestDto paymentRequestDto, UserModel userModel);
    Optional<PaymentModel> findLastPaymentByUser(UserModel userModel);
    Page<PaymentModel> findAllByUser(Specification<PaymentModel> spec, Pageable pageable);
    Optional<PaymentModel> findPaymentByUser(UUID userId, UUID paymentId);
    void makePayment(PaymentCommandDto paymentCommandDto);
}