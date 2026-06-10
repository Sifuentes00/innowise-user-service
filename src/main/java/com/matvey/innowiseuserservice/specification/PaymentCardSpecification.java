package com.matvey.innowiseuserservice.specification;

import com.matvey.innowiseuserservice.entity.PaymentCard;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

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

    public static Specification<PaymentCard> byUserNameAndSurname(String name, String surname) {
        return (root, query, cb) -> {
            if (name == null && surname == null) {
                return cb.conjunction();
            }
            ArrayList<Predicate> predicates = new ArrayList<>();
            
            if (name != null) {
                predicates.add(cb.like(cb.lower(root.join("user").get("name")), "%" + name.toLowerCase() + "%"));
            }
            if (surname != null) {
                predicates.add(cb.like(cb.lower(root.join("user").get("surname")), "%" + surname.toLowerCase() + "%"));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
