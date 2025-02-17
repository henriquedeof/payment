package com.xpto.distancelearning.payment.services.impl;

import com.xpto.distancelearning.payment.dtos.PaymentCommandDto;
import com.xpto.distancelearning.payment.dtos.PaymentRequestDto;
import com.xpto.distancelearning.payment.enums.PaymentControl;
import com.xpto.distancelearning.payment.enums.PaymentStatus;
import com.xpto.distancelearning.payment.models.CreditCardModel;
import com.xpto.distancelearning.payment.models.PaymentModel;
import com.xpto.distancelearning.payment.models.UserModel;
import com.xpto.distancelearning.payment.publishers.PaymentCommandPublisher;
import com.xpto.distancelearning.payment.publishers.PaymentEventPublisher;
import com.xpto.distancelearning.payment.repositories.CreditCardRepository;
import com.xpto.distancelearning.payment.repositories.PaymentRepository;
import com.xpto.distancelearning.payment.repositories.UserRepository;
import com.xpto.distancelearning.payment.services.PaymentService;
import com.xpto.distancelearning.payment.services.PaymentStripeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LogManager.getLogger(PaymentServiceImpl.class);

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCommandPublisher paymentCommandPublisher;

    @Autowired
    private PaymentStripeService paymentStripeService;

    @Autowired
    private PaymentEventPublisher paymentEventPublisher;

    @Transactional
    @Override
    public PaymentModel requestPayment(PaymentRequestDto paymentRequestDto, UserModel userModel) {
//        return null;

        var creditCardModel = new CreditCardModel();
        var creditCardModelOptional = creditCardRepository.findByUser(userModel);

        if(creditCardModelOptional.isPresent()){
            creditCardModel = creditCardModelOptional.get();
        }
        BeanUtils.copyProperties(paymentRequestDto, creditCardModel);
        creditCardModel.setUser(userModel);
        creditCardRepository.save(creditCardModel);

        var paymentModel = new PaymentModel();
        paymentModel.setPaymentControl(PaymentControl.REQUESTED);
        paymentModel.setPaymentRequestDate(LocalDateTime.now(ZoneId.of("UTC")));
        paymentModel.setPaymentExpirationDate(LocalDateTime.now(ZoneId.of("UTC")).plusDays(30));
        paymentModel.setLastDigitsCreditCard(paymentRequestDto.getCreditCardNumber().substring(paymentRequestDto.getCreditCardNumber().length()-4));
        paymentModel.setValuePaid(paymentRequestDto.getValuePaid());
        paymentModel.setUser(userModel);
        paymentRepository.save(paymentModel);

        try {
            var paymentCommandDto = new PaymentCommandDto();
            paymentCommandDto.setUserId(userModel.getUserId());
            paymentCommandDto.setPaymentId(paymentModel.getPaymentId());
            paymentCommandDto.setCardId(creditCardModel.getCardId());
            paymentCommandPublisher.publishPaymentCommand(paymentCommandDto);
        } catch (Exception e) {
            logger.warn("Error sending payment command!");
        }
        return paymentModel;
    }

    @Override
    public Optional<PaymentModel> findLastPaymentByUser(UserModel userModel) {
        return paymentRepository.findTopByUserOrderByPaymentRequestDateDesc(userModel);
    }

    @Override
    public Page<PaymentModel> findAllByUser(Specification<PaymentModel> spec, Pageable pageable) {
        return paymentRepository.findAll(spec, pageable);
    }

    @Override
    public Optional<PaymentModel> findPaymentByUser(UUID userId, UUID paymentId) {
        return paymentRepository.findPaymentByUser(userId, paymentId);
    }

    @Transactional
    @Override
    public void makePayment(PaymentCommandDto paymentCommandDto) {
        var paymentModel = paymentRepository.findById(paymentCommandDto.getPaymentId()).get();
        var userModel = userRepository.findById(paymentCommandDto.getUserId()).get();
        var creditCardModel = creditCardRepository.findById(paymentCommandDto.getCardId()).get();

        paymentModel = paymentStripeService.processStripePayment(paymentModel, creditCardModel);
        paymentRepository.save(paymentModel);

        if (paymentModel.getPaymentControl().equals(PaymentControl.EFFECTED)) {
            userModel.setPaymentStatus(PaymentStatus.PAYING);
            userModel.setLastPaymentDate(LocalDateTime.now(ZoneId.of("UTC")));
            userModel.setPaymentExpirationDate(LocalDateTime.now(ZoneId.of("UTC")).plusDays(30));
            if (userModel.getFirstPaymentDate() == null) {
                userModel.setFirstPaymentDate(LocalDateTime.now(ZoneId.of("UTC")));
            }
        } else {
            userModel.setPaymentStatus(PaymentStatus.DEBTOR);
        }
        userRepository.save(userModel);

        if (paymentModel.getPaymentControl().equals(PaymentControl.EFFECTED) || paymentModel.getPaymentControl().equals(PaymentControl.REFUSED)) {
            paymentEventPublisher.publishPaymentEvent(paymentModel.convertToPaymentEventDto());
        } else if (paymentModel.getPaymentControl().equals(PaymentControl.ERROR)) {
            //retry process and limits retry
        }
    }
}