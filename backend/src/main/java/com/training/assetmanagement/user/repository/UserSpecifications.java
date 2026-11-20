package com.training.assetmanagement.user.repository;

import com.training.assetmanagement.organization.domain.OrgTree;
import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserAttribute;
import com.training.assetmanagement.user.domain.UserFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.*;

// Bộ lọc danh sách người dùng - kết hợp AND với viewSpec(USER_MANAGE)
public final class UserSpecifications {

    private static final char LIKE_ESCAPE = '\\';

    public static Specification<User> hasId(UUID id) {
        return (root, query, cb) -> cb.equal(root.get(UserAttribute.ID.getPath()), id);
    }

    // roleUserIds = null: Không lọc theo vai trò
    public static Specification<User> matching(UserFilter filter, OrgTree tree, Set<UUID> roleUserIds) {
        return (root, query, cb) -> {
            List<Predicate> all = new ArrayList<>();
            if (filter.q() != null && !filter.q().isBlank()) {
                // Collation _ci: LIKE không phân biệt hoa thường, dấu
                String pattern = "%" + escapeLike(filter.q().trim()) + "%";
                all.add(cb.or(
                    cb.like(root.get(UserAttribute.USERNAME.getPath()), pattern, LIKE_ESCAPE),
                    cb.like(root.get(UserAttribute.FULL_NAME.getPath()), pattern, LIKE_ESCAPE),
                    cb.like(root.get(UserAttribute.EMAIL.getPath()), pattern, LIKE_ESCAPE)
                ));
            }
            if (filter.orgUnitId() != null) {
                // Cả cây con: Lọc theo chi nhánh ra mọi phòng ban bên dưới
                all.add(in(cb, root.get(UserAttribute.ORG_UNIT_ID.getPath()), tree.subtreeIds(filter.orgUnitId())));
            }
            if (roleUserIds != null) {
                all.add(in(cb, root.get(UserAttribute.ID.getPath()), roleUserIds));
            }
            if (filter.status() != null) {
                all.add(cb.equal(root.get(UserAttribute.STATUS.getPath()), filter.status()));
            }
            return cb.and(all.toArray(Predicate[]::new));
        };
    }

    // Tập rỗng -> không dòng nào; không bao giờ sinh IN ()
    private static Predicate in(CriteriaBuilder cb, Path<UUID> path, Collection<UUID> values) {
        return values.isEmpty() ? cb.disjunction() : path.in(values);
    }

    private static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private UserSpecifications() {}
}
