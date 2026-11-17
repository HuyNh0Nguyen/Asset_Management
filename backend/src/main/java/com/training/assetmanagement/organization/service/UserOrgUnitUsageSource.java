package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.organization.domain.OrgUnitBlocker;
import com.training.assetmanagement.organization.domain.OrgUnitBlockerItem;
import com.training.assetmanagement.organization.domain.OrgUnitBlockerKind;
import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.domain.UserStatus;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Vướng từ bảng users: Ngừng hoạt động - người ACTIVE; xóa - mọi người (người dùng không bao giờ bị xóa)
@Service
@RequiredArgsConstructor
public class UserOrgUnitUsageSource implements OrgUnitUsageSource {

    private static final Limit SAMPLE_LIMIT = Limit.of(OrgUnitBlocker.SAMPLE_LIMIT);

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitBlocker> deactivationBlockers(UUID unitId) {
        long total = userRepository.countByOrgUnitIdAndStatus(unitId, UserStatus.ACTIVE);
        if (total == 0) {
            return List.of();
        }
        List<OrgUnitBlockerItem> samples = userRepository
            .findByOrgUnitIdAndStatusOrderByUsernameAsc(unitId, UserStatus.ACTIVE, SAMPLE_LIMIT)
            .stream()
            .map(UserOrgUnitUsageSource::toItem)
            .toList();
        return List.of(new OrgUnitBlocker(OrgUnitBlockerKind.ACTIVE_USER, total, samples));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrgUnitBlocker> deletionBlockers(UUID unitId) {
        long total = userRepository.countByOrgUnitId(unitId);
        if (total == 0) {
            return List.of();
        }
        List<OrgUnitBlockerItem> samples = userRepository.findByOrgUnitIdOrderByUsernameAsc(unitId, SAMPLE_LIMIT)
            .stream()
            .map(UserOrgUnitUsageSource::toItem)
            .toList();
        return List.of(new OrgUnitBlocker(OrgUnitBlockerKind.USER, total, samples));
    }

    // code = username, name = họ tên; người đã nghỉ mang inactive = true
    private static OrgUnitBlockerItem toItem(User user) {
        return new OrgUnitBlockerItem(user.getId(), user.getUsername(), user.getFullName(),
            user.getStatus() != UserStatus.ACTIVE);
    }
}
