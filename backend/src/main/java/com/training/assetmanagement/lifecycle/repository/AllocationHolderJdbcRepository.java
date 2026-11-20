package com.training.assetmanagement.lifecycle.repository;

import com.training.assetmanagement.lifecycle.domain.AllocationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

// TODO: JDBC vì entity Allocation tới phase 7 mới có; phase 7 đổi sang JPA, cổng giữ nguyên.
// Chạy chung giao dịch JPA đang mở; dùng chỉ mục idx_allocations_holder_status
@Repository
@RequiredArgsConstructor
public class AllocationHolderJdbcRepository {

    private static final String COUNT_OPEN = """
        SELECT COUNT(*) FROM allocations
        WHERE holder_id = :holderId AND status = :status
        """;
    // Mã + tên tài sản / lô, số lượng đang giữ
    private static final String SAMPLE_OPEN = """
        SELECT al.id, COALESCE(a.code, b.code) AS code, COALESCE(a.name, b.name) AS name,
               al.quantity - al.returned_quantity AS holding
        FROM allocations al
        LEFT JOIN assets a ON a.id = al.asset_id
        LEFT JOIN asset_batches b ON b.id = al.batch_id
        WHERE al.holder_id = :holderId AND al.status = :status
        ORDER BY code
        LIMIT :limit
        """;

    private final JdbcClient jdbcClient;

    public long countOpen(UUID holderId) {
        return jdbcClient.sql(COUNT_OPEN)
            .param("holderId", holderId.toString())
            .param("status", AllocationStatus.OPEN.name())
            .query(Long.class)
            .single();
    }

    public List<AllocationHolderRow> sampleOpen(UUID holderId, int limit) {
        return jdbcClient.sql(SAMPLE_OPEN)
            .param("holderId", holderId.toString())
            .param("status", AllocationStatus.OPEN.name())
            .param("limit", limit)
            .query(AllocationHolderJdbcRepository::toRow)
            .list();
    }

    private static AllocationHolderRow toRow(ResultSet rs, int rowNum) throws SQLException {
        return new AllocationHolderRow(UUID.fromString(rs.getString("id")), rs.getString("code"),
            rs.getString("name"), rs.getInt("holding"));
    }
}
