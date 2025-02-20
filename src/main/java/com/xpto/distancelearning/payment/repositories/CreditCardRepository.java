package com.xpto.distancelearning.payment.repositories;

import com.xpto.distancelearning.payment.models.CreditCardModel;
import com.xpto.distancelearning.payment.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CreditCardRepository extends JpaRepository<CreditCardModel, UUID>, JpaSpecificationExecutor<CreditCardModel> {
    Optional<CreditCardModel> findByUser(UserModel userModel);
}