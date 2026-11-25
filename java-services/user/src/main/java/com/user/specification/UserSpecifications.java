package com.user.specification;

import com.user.entity.UserProfile;
import com.user.entity.UserType;
import org.springframework.data.jpa.domain.Specification;

public final class UserSpecifications {

    private UserSpecifications() {
    }

    public static Specification<UserProfile> hasUserType(UserType userType) {
        return (root, query, cb) -> cb.equal(root.get("userType"), userType);
    }

    public static Specification<UserProfile> matchesKeyword(String keyword) {
        String pattern = "%" + keyword.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("lastName"), "")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("username"), "")), pattern));
    }
}
