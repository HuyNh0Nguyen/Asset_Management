package com.training.assetmanagement.access.domain;

// Tương đối theo đơn vị của người dùng
public enum DataScope {
    SELF,
    DEPARTMENT,
    BRANCH,
    ALL;

    // Hợp phạm vi qua các vai trò: Lấy phạm vi rộng hơn
    public static DataScope widest(DataScope first, DataScope second) {
        return first.breadth() >= second.breadth() ? first : second;
    }

    // Độ rộng khai tường minh - không dựa vào ordinal(
    private int breadth() {
        return switch (this) {
            case SELF -> 0;
            case DEPARTMENT -> 1;
            case BRANCH -> 2;
            case ALL -> 3;
        };
    }
}
