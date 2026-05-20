package com.matvey.innowiseuserservice.specification;

import com.matvey.innowiseuserservice.entity.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class UserSpecification {

    public static Specification<User> byNameAndSurname(String name, String surname) {
        return (root, query, cb) -> {
            if (name == null && surname == null) {
                return cb.conjunction();
            }
            if (name == null) {
                return cb.equal(root.get("surname"), surname);
            }
            if (surname == null) {
                return cb.equal(root.get("name"), name);
            }
            return cb.and(
                cb.equal(root.get("name"), name),
                cb.equal(root.get("surname"), surname)
            );
        };
    }

    public static Specification<User> byActive(Boolean active) {
        return (root, query, cb) -> {
            if (active == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("active"), active);
        };
    }
}
