package com.training.assetmanagement.audit.service;

import com.training.assetmanagement.audit.domain.AuditActor;
import com.training.assetmanagement.audit.domain.AuditEvent;
import com.training.assetmanagement.audit.domain.AuditLogRow;
import com.training.assetmanagement.audit.repository.AuditLogJdbcRepository;
import com.training.assetmanagement.shared.persistence.UuidV7;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLoggerImpl implements AuditLogger {

    private final AuditLogJdbcRepository auditLogJdbcRepository;
    private final JsonMapper jsonMapper;
    private final Clock clock;

    // MANDATORY: Audit chỉ ghi cùng giao dịch nghiệp vụ - rollback thì Audit rollback theo
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void log(AuditActor actor, AuditEvent event) {
        auditLogJdbcRepository.insert(new AuditLogRow(
            UuidV7.generate(clock),
            actor.actorId(),
            jsonMapper.writeValueAsString(actor.roleCodes()),
            event.action(),
            event.targetType(),
            event.targetId(),
            event.targetCode(),
            toJsonOrNull(event.oldValue()),
            toJsonOrNull(event.newValue()),
            LocalDateTime.now(clock)
        ));
    }

    private String toJsonOrNull(Map<String, Object> value) {
        return value == null || value.isEmpty() ? null : jsonMapper.writeValueAsString(value);
    }
}
