package com.training.assetmanagement.access.domain;

// Ba kết cục: Ngoài vị từ xem -> 404; xem được nhưng không tác động được -> 403
public enum ScopeDecision {
    NOT_VISIBLE,
    VISIBLE_ONLY,
    ALLOWED
}
