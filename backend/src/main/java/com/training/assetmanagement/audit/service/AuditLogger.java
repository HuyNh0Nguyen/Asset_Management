package com.training.assetmanagement.audit.service;

import com.training.assetmanagement.audit.domain.AuditActor;
import com.training.assetmanagement.audit.domain.AuditEvent;

public interface AuditLogger {

    // Actor truyển tường minh: Đăng nhập chưa có CurrentActor; audit cũng không phụ thuộc access
    void log(AuditActor actor, AuditEvent event);
}
