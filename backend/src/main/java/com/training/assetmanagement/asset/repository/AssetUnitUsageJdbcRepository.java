package com.training.assetmanagement.asset.repository;

import com.training.assetmanagement.asset.domain.AssetStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

// TODO: JDBC vì entity Asset / AssetBatch tới phase 3 mới có; phase 3 đổi sang JPA, cổng giữ nguyên.
// Chạy chung giao dịch JPA đang mở (JpaTransactionManager chia sẻ connection)
@Repository
@RequiredArgsConstructor
public class AssetUnitUsageJdbcRepository {

    // Tài sản cá biệt: "còn sống" = chưa DISPOSED
    private static final String COUNT_ASSETS = """
        SELECT COUNT(*) FROM assets
        WHERE managing_unit_id = :unitId AND (:liveOnly = FALSE OR status <> :disposed)
        """;
    private static final String SAMPLE_ASSETS = """
        SELECT id, code, name, status = :disposed AS disposed FROM assets
        WHERE managing_unit_id = :unitId AND (:liveOnly = FALSE OR status <> :disposed)
        ORDER BY code
        LIMIT :limit
        """;

    // Lô: "còn sống" = chưa thanh lý hết
    private static final String COUNT_BATCHES = """
        SELECT COUNT(*) FROM asset_batches
        WHERE managing_unit_id = :unitId AND (:liveOnly = FALSE OR disposed_quantity < quantity)
        """;
    private static final String SAMPLE_BATCHES = """
        SELECT id, code, name, disposed_quantity >= quantity AS disposed FROM asset_batches
        WHERE managing_unit_id = :unitId AND (:liveOnly = FALSE OR disposed_quantity < quantity)
        ORDER BY code
        LIMIT :limit
        """;

    private final JdbcClient jdbcClient;

    public long countAssets(UUID unitId, boolean liveOnly) {
        return jdbcClient.sql(COUNT_ASSETS)
            .param("unitId", unitId.toString())
            .param("liveOnly", liveOnly)
            .param("disposed", AssetStatus.DISPOSED.name())
            .query(Long.class)
            .single();
    }

    public List<AssetUsageRow> sampleAssets(UUID unitId, boolean liveOnly, int limit) {
        return jdbcClient.sql(SAMPLE_ASSETS)
            .param("unitId", unitId.toString())
            .param("liveOnly", liveOnly)
            .param("disposed", AssetStatus.DISPOSED.name())
            .param("limit", limit)
            .query(AssetUnitUsageJdbcRepository::toRow)
            .list();
    }

    public long countBatches(UUID unitId, boolean liveOnly) {
        return jdbcClient.sql(COUNT_BATCHES)
            .param("unitId", unitId.toString())
            .param("liveOnly", liveOnly)
            .query(Long.class)
            .single();
    }

    public List<AssetUsageRow> sampleBatches(UUID unitId, boolean liveOnly, int limit) {
        return jdbcClient.sql(SAMPLE_BATCHES)
            .param("unitId", unitId.toString())
            .param("liveOnly", liveOnly)
            .param("limit", limit)
            .query(AssetUnitUsageJdbcRepository::toRow)
            .list();
    }

    private static AssetUsageRow toRow(ResultSet rs, int rowNum) throws SQLException {
        return new AssetUsageRow(UUID.fromString(rs.getString("id")), rs.getString("code"),
            rs.getString("name"), rs.getBoolean("disposed"));
    }
}
