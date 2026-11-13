package com.training.assetmanagement.audit.repository;

import com.training.assetmanagement.audit.domain.AuditLogRow;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

// Ghi bằng JDBC: Hibernate 7 chưa có FormatMapper JSON cho Jackson 3.
// Chạy chung giao dịch JPA đang mở (JpaTransactionManager chia sẻ connection)
@Repository
@RequiredArgsConstructor
public class AuditLogJdbcRepository {

    private static final String INSERT_SQL = """
        INSERT INTO audit_logs (id, actor_id, actor_roles, action, target_type, target_id,
                                        target_code, old_value, new_value, created_at)
                VALUES (:id, :actorId, :actorRoles, :action, :targetType, :targetId,
                        :targetCode, :oldValue, :newValue, :createdAt)
        """;

    private final JdbcClient jdbcClient;

    public void insert(AuditLogRow row) {
        jdbcClient.sql(INSERT_SQL)
            .param("id", row.id().toString())
            .param("actorId", row.actorId().toString())
            .param("actorRoles", row.actorRolesJson())
            .param("action", row.action().name())
            .param("targetType", row.targetType().name())
            .param("targetId", row.targetId().toString())
            .param("targetCode", row.targetCode())
            .param("oldValue", row.oldValueJson())
            .param("newValue", row.newValueJson())
            .param("createdAt", row.createdAt())
            .update();
    }
}
