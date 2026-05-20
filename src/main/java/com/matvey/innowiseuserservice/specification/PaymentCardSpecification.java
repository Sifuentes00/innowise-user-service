package com.matvey.innowiseuserservice.specification;

import com.matvey.innowiseuserservice.entity.PaymentCard;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

    public static Specification<PaymentCard> byHolder(String holder) {
        return (root, query, cb) -> {
            if (holder == null) {
                return cb.conjunction();
            }
            return cb.like(cb.lower(root.get("holder")), "%" + holder.toLowerCase() + "%");
        };
    }

    public static Specification<PaymentCard> byActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("active"), active);
        };
    }

    public static Specification<PaymentCard> byUserId(java.util.UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("user").get("id"), userId);
        };
    }
}
