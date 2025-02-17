package com.xpto.distancelearning.payment.repositories;

import com.xpto.distancelearning.payment.models.PaymentModel;
import com.xpto.distancelearning.payment.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentModel, UUID>, JpaSpecificationExecutor<PaymentModel> {

    // 'TopBy' is a keyword that is used to get the first element of the list, and 'User' is a field in the PaymentModel class (PaymentModel.user).
    // 'OrderBy' is a keyword that is used to sort the list.
    // 'PaymentRequestDate' is a field in the PaymentModel class (PaymentModel.paymentRequestDate).
    // 'Desc' is a keyword that is used to sort the list in descending order.
    Optional<PaymentModel> findTopByUserOrderByPaymentRequestDateDesc(UserModel userModel);

    @Query(value = "select * from tb_payments where user_user_id = :userId and payment_id = :paymentId", nativeQuery = true)
    Optional<PaymentModel> findPaymentByUser(@Param("userId") UUID userId, @Param("paymentId") UUID paymentId);
}