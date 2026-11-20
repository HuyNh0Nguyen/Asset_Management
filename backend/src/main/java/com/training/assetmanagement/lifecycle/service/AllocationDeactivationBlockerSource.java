package com.training.assetmanagement.lifecycle.service;

import com.training.assetmanagement.lifecycle.repository.AllocationHolderJdbcRepository;
import com.training.assetmanagement.user.domain.UserBlocker;
import com.training.assetmanagement.user.domain.UserBlockerItem;
import com.training.assetmanagement.user.domain.UserBlockerKind;
import com.training.assetmanagement.user.service.UserDeactivationBlockerSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

// Còn bản ghi cấp phát OPEN do người này giữ -> chăn
@Service
@RequiredArgsConstructor
public class AllocationDeactivationBlockerSource implements UserDeactivationBlockerSource {

    private final AllocationHolderJdbcRepository allocationHolderJdbcRepository;

    @Override
    public List<UserBlocker> deactivationBlockers(UUID userId) {
        long total = allocationHolderJdbcRepository.countOpen(userId);
        if (total == 0) {
            return List.of();
        }
        List<UserBlockerItem> samples = allocationHolderJdbcRepository.sampleOpen(userId, UserBlocker.SAMPLE_LIMIT)
            .stream()
            .map(row -> new UserBlockerItem(row.id(), row.code(), row.name(), String.valueOf(row.holding())))
            .toList();
        return List.of(new UserBlocker(UserBlockerKind.OPEN_ALLOCATION, total, samples));
    }
}
