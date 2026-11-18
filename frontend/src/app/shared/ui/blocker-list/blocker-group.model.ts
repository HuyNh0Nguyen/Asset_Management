// Một nhóm thứ đang chặn thao tác - không biết miền (đơn vị, người dùng... tự ánh xạ sang)
export interface BlockerGroup {
    key: string;
    label: string;
    hint?: string;
    // Tổng đếm đúng; items có thể chỉ là mẫu
    total: number;
    items: BlockerGroupItem[];
}

export interface BlockerGroupItem {
    key: string;
    primary: string;
    secondary?: string;
    inactive: boolean;
}