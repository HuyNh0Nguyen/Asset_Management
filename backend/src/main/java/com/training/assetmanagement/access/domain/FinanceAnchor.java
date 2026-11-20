package com.training.assetmanagement.access.domain;

// ASSET_FINANCE_VIEW phải phủ đơn vị nào
public enum FinanceAnchor {
    ANY_SCOPE,              // Có quyền ở bất kỳ phạm vi nào
    MANAGING_UNIT,          // Đơn vị quản lý hiện tại
    REQUESTER_UNIT,         // Đơn vị người đề xuất + ngoại lệ ứng viên
}
