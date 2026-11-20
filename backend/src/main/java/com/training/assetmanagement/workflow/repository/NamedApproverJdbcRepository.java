package com.training.assetmanagement.workflow.repository;

import com.training.assetmanagement.workflow.domain.ApproverRuleType;
import com.training.assetmanagement.workflow.domain.WorkflowStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

// TODO: JDBC vì entity Workflow tới phase 5 mới có; phase 5 đổi sang JPA, cổng giữ nguyên
@Repository
@RequiredArgsConstructor
public class NamedApproverJdbcRepository {

    private static final String FROM_ACTIVE_USER_RULES = """
        FROM workflow_approver_rules r
        JOIN workflow_stages s ON s.id = r.stage_id
        JOIN workflows w ON w.id = s.workflow_id
        WHERE r.rule_type = :ruleType AND r.user_id = :userId AND w.status = :status
        """;
    private static final String COUNT = "SELECT COUNT(*) " + FROM_ACTIVE_USER_RULES;
    private static final String SAMPLE = """
        SELECT r.id AS rule_id, w.code AS workflow_code, w.name AS workflow_name, s.name AS stage_name
        """ + FROM_ACTIVE_USER_RULES + """
        ORDER BY w.code, s.stage_order
        LIMIT :limit
        """;

    private final JdbcClient jdbcClient;

    public long countActiveRules(UUID userId) {
        return jdbcClient.sql(COUNT)
            .param("ruleType", ApproverRuleType.USER.name())
            .param("userId", userId.toString())
            .param("status", WorkflowStatus.ACTIVE.name())
            .query(Long.class)
            .single();
    }

    public List<NamedApproverRow> sampleActiveRules(UUID userId, int limit) {
        return jdbcClient.sql(SAMPLE)
            .param("ruleType", ApproverRuleType.USER.name())
            .param("userId", userId.toString())
            .param("status", WorkflowStatus.ACTIVE.name())
            .param("limit", limit)
            .query(NamedApproverJdbcRepository::toRow)
            .list();
    }

    private static NamedApproverRow toRow(ResultSet rs, int rowNum) throws SQLException {
        return new NamedApproverRow(UUID.fromString(rs.getString("rule_id")), rs.getString("workflow_code"),
            rs.getString("workflow_name"), rs.getString("stage_name"));
    }
}
