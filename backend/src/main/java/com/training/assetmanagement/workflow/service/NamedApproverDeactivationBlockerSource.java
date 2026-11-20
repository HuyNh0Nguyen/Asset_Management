package com.training.assetmanagement.workflow.service;

import com.training.assetmanagement.user.domain.UserBlocker;
import com.training.assetmanagement.user.domain.UserBlockerItem;
import com.training.assetmanagement.user.domain.UserBlockerKind;
import com.training.assetmanagement.user.service.UserDeactivationBlockerSource;
import com.training.assetmanagement.workflow.repository.NamedApproverJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Được chỉ định đích danh (quy tắc USER) trong quy trình ACTIIVE -> chặn
@Service
@RequiredArgsConstructor
public class NamedApproverDeactivationBlockerSource implements UserDeactivationBlockerSource {

    private final NamedApproverJdbcRepository namedApproverJdbcRepository;

    @Override
    public List<UserBlocker> deactivationBlockers(UUID userId) {
        long total = namedApproverJdbcRepository.countActiveRules(userId);
        if (total == 0) {
            return List.of();
        }
        List<UserBlockerItem> samples = namedApproverJdbcRepository.sampleActiveRules(userId, UserBlocker.SAMPLE_LIMIT)
            .stream()
            .map(row -> new UserBlockerItem(row.ruleId(), row.workflowCode(), row.workflowName(), row.stageName()))
            .toList();
        return List.of(new UserBlocker(UserBlockerKind.NAMED_APPROVER, total, samples));
    }
}
