package com.training.assetmanagement.asset.service;

import com.training.assetmanagement.asset.repository.AssetUnitUsageJdbcRepository;
import com.training.assetmanagement.asset.repository.AssetUsageRow;
import com.training.assetmanagement.organization.domain.OrgUnitBlocker;
import com.training.assetmanagement.organization.domain.OrgUnitBlockerItem;
import com.training.assetmanagement.organization.domain.OrgUnitBlockerKind;
import com.training.assetmanagement.organization.service.OrgUnitUsageSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToLongFunction;

// Vướng từ assets / asset_batches theo đơn vị quản lý
// Ngừng hoạt động: Tài sản chưa DISPOSED, lô chưa thanh lý hết. Xóa: Mọi tài sản / lô
@Service
@RequiredArgsConstructor
public class AssetOrgUnitUsageSource implements OrgUnitUsageSource {

    private final AssetUnitUsageJdbcRepository usageRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitBlocker> deactivationBlockers(UUID unitId) {
        return collect(unitId, true, OrgUnitBlockerKind.LIVE_ASSET, OrgUnitBlockerKind.LIVE_BATCH);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitBlocker> deletionBlockers(UUID unitId) {
        return collect(unitId, false, OrgUnitBlockerKind.ASSET, OrgUnitBlockerKind.BATCH);
    }

    private List<OrgUnitBlocker> collect(UUID unitId, boolean liveOnly, OrgUnitBlockerKind assetKind,
                                         OrgUnitBlockerKind batchKind) {
        List<OrgUnitBlocker> blockers = new ArrayList<>();
        addIfAny(blockers, assetKind,
            id -> usageRepository.countAssets(id, liveOnly),
            id -> usageRepository.sampleAssets(id, liveOnly, OrgUnitBlocker.SAMPLE_LIMIT), unitId);
        addIfAny(blockers, batchKind,
            id -> usageRepository.countBatches(id, liveOnly),
            id -> usageRepository.sampleBatches(id, liveOnly, OrgUnitBlocker.SAMPLE_LIMIT), unitId);
        return blockers;
    }

    private static void addIfAny(List<OrgUnitBlocker> blockers, OrgUnitBlockerKind kind,
                                 ToLongFunction<UUID> counter, Function<UUID, List<AssetUsageRow>> sampler,
                                 UUID unitId) {
        long total = counter.applyAsLong(unitId);
        if (total == 0) {
            return;
        }
        List<OrgUnitBlockerItem> samples = sampler.apply(unitId).stream()
            .map(row -> new OrgUnitBlockerItem(row.id(), row.code(), row.name(), row.disposed()))
            .toList();
        blockers.add(new OrgUnitBlocker(kind, total, samples));
    }
}
