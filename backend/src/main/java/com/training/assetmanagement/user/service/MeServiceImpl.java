package com.training.assetmanagement.user.service;

import com.training.assetmanagement.access.domain.ActorContext;
import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.organization.domain.OrgUnit;
import com.training.assetmanagement.organization.domain.Rank;
import com.training.assetmanagement.organization.service.OrganizationLookupService;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import com.training.assetmanagement.user.domain.MeProfile;
import com.training.assetmanagement.user.domain.User;
import com.training.assetmanagement.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeServiceImpl implements MeService {

    private final CurrentActor currentActor;
    private final UserRepository userRepository;
    private final OrganizationLookupService organizationLookupService;

    @Override
    @Transactional(readOnly = true)
    public MeProfile getMe() {
        ActorContext actor = currentActor.get();
        User user = userRepository.findById(actor.userId())
            .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        OrgUnit orgUnit = organizationLookupService.findOrgUnit(user.getOrgUnitId())
            .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Org unit missing for user " + user.getId()));
        Rank rank = organizationLookupService.findRank(user.getRankId())
            .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Rank missing for user " + user.getId()));
        // Quyền lấy từ ActorContext đã nạp ở filter - không truy vấn lần hai
        return new MeProfile(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(),
            actor.mustChangePassword(), orgUnit.getId(), orgUnit.getCode(), orgUnit.getName(),
            rank.getName(), actor.grants());
    }
}
