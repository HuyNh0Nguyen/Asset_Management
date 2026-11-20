package com.training.assetmanagement.organization.service;

import com.training.assetmanagement.access.service.CurrentActor;
import com.training.assetmanagement.audit.domain.AuditAction;
import com.training.assetmanagement.audit.domain.AuditChanges;
import com.training.assetmanagement.audit.domain.AuditTargetType;
import com.training.assetmanagement.organization.domain.OrgAuditField;
import com.training.assetmanagement.organization.domain.OrgConstraint;
import com.training.assetmanagement.organization.domain.Rank;
import com.training.assetmanagement.organization.repository.RankRepository;
import com.training.assetmanagement.shared.persistence.ConstraintViolations;
import com.training.assetmanagement.shared.persistence.UuidV7;
import com.training.assetmanagement.shared.web.BusinessException;
import com.training.assetmanagement.shared.web.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RankServiceImpl implements RankService {

    private final RankRepository rankRepository;
    private final OrgAuditService orgAuditService;
    private final CurrentActor currentActor;
    private final Clock clock;

    @Override
    @Transactional(readOnly = true)
    public List<Rank> list() {
        return rankRepository.findAllByOrderBySeniorityAsc();
    }

    @Override
    @Transactional
    public Rank create(String code, String name, int seniority) {
        if (rankRepository.existsByCode(code)) {
            throw new BusinessException(ErrorCode.RANK_CODE_DUPLICATE);
        }
        if (rankRepository.existsBySeniority(seniority)) {
            throw new BusinessException(ErrorCode.RANK_SENIORITY_DUPLICATE);
        }
        Rank rank = Rank.create(UuidV7.generate(clock), code, name.strip(), seniority,
            currentActor.get().userId(), LocalDateTime.now(clock));
        saveAndFlush(rank);

        AuditChanges changes = new AuditChanges()
            .putNew(OrgAuditField.CODE, rank.getCode())
            .putNew(OrgAuditField.NAME, rank.getName())
            .putNew(OrgAuditField.SENIORITY, rank.getSeniority());
        orgAuditService.log(AuditAction.CREATE, AuditTargetType.RANK, rank.getId(), rank.getCode(), changes);
        return rank;
    }

    @Override
    @Transactional
    public Rank update(UUID id, String name, int seniority) {
        Rank rank = rankRepository.findByIdForUpdate(id).orElseThrow(RankServiceImpl::notFound);
        String newName= name.strip();
        AuditChanges changes = new AuditChanges()
            .track(OrgAuditField.NAME, rank.getName(), newName)
            .track(OrgAuditField.SENIORITY, rank.getSeniority(), seniority);
        if (changes.isEmpty()) {
            return rank;
        }
        // Đổi chỗ hai bậc phải qua một số trung gian - ràng buộc duy nhất kiểm từng dòng
        if (rank.getSeniority() != seniority && rankRepository.existsBySeniorityAndIdNot(seniority, id)) {
            throw new BusinessException(ErrorCode.RANK_SENIORITY_DUPLICATE);
        }
        rank.rename(newName);
        rank.changeSeniority(seniority);
        rank.markUpdated(currentActor.get().userId(), LocalDateTime.now(clock));
        saveAndFlush(rank);
        orgAuditService.log(AuditAction.UPDATE, AuditTargetType.RANK, rank.getId(), rank.getCode(), changes);
        return rank;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Rank rank = rankRepository.findByIdForUpdate(id).orElseThrow(RankServiceImpl::notFound);
        // Audit trước lệnh xóa, cùng giao dịch: Bản chụp toàn dòng
        AuditChanges snapshot = new AuditChanges()
            .putOld(OrgAuditField.CODE, rank.getCode())
            .putOld(OrgAuditField.NAME, rank.getName())
            .putOld(OrgAuditField.SENIORITY, rank.getSeniority());
        orgAuditService.log(AuditAction.DELETE, AuditTargetType.RANK, rank.getId(), rank.getCode(), snapshot);
        try {
            rankRepository.delete(rank);
            rankRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.RANK_IN_USE, "Rank " + id + " is still referenced");
        }
    }

    // Hai Admin ghi cùng mã / số thứ bậc cùng lúc -> đổi vi phạm duy nhất thành mã nghiệp vụ
    private void saveAndFlush(Rank rank) {
        try {
            rankRepository.saveAndFlush(rank);
        } catch (DataIntegrityViolationException ex) {
            if (ConstraintViolations.violates(ex, OrgConstraint.UK_RANKS_CODE.getConstraintName())) {
                throw new BusinessException(ErrorCode.RANK_CODE_DUPLICATE);
            }
            if (ConstraintViolations.violates(ex, OrgConstraint.UK_RANKS_SENIORITY.getConstraintName())) {
                throw new BusinessException(ErrorCode.RANK_SENIORITY_DUPLICATE);
            }
            throw ex;
        }
    }

    private static BusinessException notFound() {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
