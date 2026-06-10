package com.matvey.innowiseuserservice.repository;

import com.matvey.innowiseuserservice.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID>, JpaSpecificationExecutor<PaymentCard> {

    Optional<PaymentCard> findByNumber(String number);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    List<PaymentCard> findByUserId(@Param("userId") UUID userId);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId")
    Page<PaymentCard> findByUserId(@Param("userId") UUID userId, Pageable pageable);
}
