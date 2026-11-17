package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class OrgUnitBlockerServiceImpl implements OrgUnitBlockerService {

    // Mọi bean hiện thực cổng (user, asset, TODO phase 4 thêm lifecycle, workflow)
    private final List<OrgUnitUsageSource> usageSources;

    @Override
    public List<OrgUnitBlocker> deactivationBlockers(OrgTree tree, UUID unitId) {
        return collect(tree, unitId, OrgUnitBlockerKind.ACTIVE_CHILD_UNIT, OrgNode::isActive,
            source -> source.deactivationBlockers(unitId));
    }

    @Override
    public List<OrgUnitBlocker> deletionBlockers(OrgTree tree, UUID unitId) {
        return collect(tree, unitId, OrgUnitBlockerKind.CHILD_UNIT, child -> true,
            source -> source.deletionBlockers(unitId));
    }

    // Đơn vị con lấy từ cây đã nạp (của chính miền); phần còn lại hỏi các cổng
    private List<OrgUnitBlocker> collect(OrgTree tree, UUID unitId, OrgUnitBlockerKind childKind,
                                         Predicate<OrgNode> childFilter,
                                         Function<OrgUnitUsageSource, List<OrgUnitBlocker>> fromSource) {
        List<OrgUnitBlocker> blockers = new ArrayList<>();
        List<OrgNode> children = tree.children(unitId).stream().filter(childFilter).toList();
        if (!children.isEmpty()) {
            List<OrgUnitBlockerItem> samples = children.stream()
                .limit(OrgUnitBlocker.SAMPLE_LIMIT)
                .map(child -> new OrgUnitBlockerItem(child.id(), child.code(), child.name(), !child.isActive()))
                .toList();
            blockers.add(new OrgUnitBlocker(childKind, children.size(), samples));
        }
        usageSources.forEach(source -> blockers.addAll(fromSource.apply(source)));
        blockers.sort(Comparator.comparing(OrgUnitBlocker::kind));
        return blockers;
    }
}
