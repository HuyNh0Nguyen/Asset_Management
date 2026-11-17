package com.training.assetmanagement.organization.domain;

// Thao tác trên một nút cây - FE dựng menu từ allowedActions, không tự xét quy tắc
public enum OrgUnitAction {
    ADD_CHILD,
    EDIT,
    MOVE,
    DELETE,
    DEACTIVATE,
    REACTIVATE
}
