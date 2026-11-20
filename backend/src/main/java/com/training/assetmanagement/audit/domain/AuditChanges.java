package com.training.assetmanagement.audit.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// Gom giá trị Audit cho mọi miền: track() chỉ giữ trường thật sự đổi;
// putNew() / putOld() ghi bản chụp khi tạo / xóa
public final class AuditChanges {

    // LinkedHashMap: Giữ thứ tự khóa, chấp nhận giá trị null (VD gỡ trưởng đơn vị)
    private final Map<String, Object> oldValue = new LinkedHashMap<>();
    private final Map<String, Object> newValue = new LinkedHashMap<>();

    public AuditChanges track(AuditField field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            oldValue.put(field.getKey(), before);
            newValue.put(field.getKey(), after);
        }
        return this;
    }

    public AuditChanges putNew(AuditField field, Object value) {
        newValue.put(field.getKey(), value);
        return this;
    }

    public AuditChanges putOld(AuditField field, Object value) {
        oldValue.put(field.getKey(), value);
        return this;
    }

    public boolean isEmpty() {
        return oldValue.isEmpty() && newValue.isEmpty();
    }

    public Map<String, Object> oldValue() {
        return Collections.unmodifiableMap(oldValue);
    }

    public Map<String, Object> newValue() {
        return Collections.unmodifiableMap(newValue);
    }
}
