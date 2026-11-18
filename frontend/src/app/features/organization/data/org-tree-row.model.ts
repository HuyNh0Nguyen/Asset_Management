import { OrgUnitAction, OrgUnitNodeResponse } from "@api/models";

// Một mục của menu thao tác - dựng sẵn để template không phải tra bảng
export interface OrgActionItem {
    action: OrgUnitAction;
    label: string;
    icon: string;
    danger: boolean;
}

// Một dòng hiển thị của cây
export interface OrgTreeRow {
    node: OrgUnitNodeResponse;
    hasChildren: boolean;
    expanded: boolean;
    // Khớp từ khóa tìm kiếm (tổ tiên chỉ hiện làm ngữ cảnh)
    match: boolean;
    actions: OrgActionItem[];
    // Đang chạy hiệu ứng thu gọn
    collapsing?: boolean;
}
