package com.training.assetmanagement.organization.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

// Gom giá trị cho Audit: track() chỉ giữ trường thật sự đổi;
// putNew() / putOld() ghi bản chụp khi tạo / xóa
public final class OrgAuditChanges {

    // LinkedHashMap: Giữ thứ tự khóa, chấp nhận giá trị null (VD gỡ trưởng đơn vị)
    private final Map<String, Object> oldValue = new LinkedHashMap<>();
    private final Map<String, Object> newValue = new LinkedHashMap<>();

    public OrgAuditChanges track(OrgAuditField field, Object before, Object after) {
        if (!Objects.equals(before, after)) {
            oldValue.put(field.getKey(), before);
            newValue.put(field.getKey(), after);
        }
        return this;
    }

    public OrgAuditChanges putNew(OrgAuditField field, Object value) {
        newValue.put(field.getKey(), value);
        return this;
    }

    public OrgAuditChanges putOld(OrgAuditField field, Object value) {
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
