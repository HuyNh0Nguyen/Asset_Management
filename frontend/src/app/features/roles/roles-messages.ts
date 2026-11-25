// Câu thông báo của miền phân quyền
export enum RoleMessage {
    ROLE_SAVED = 'Đã lưu. Người giữ vai trò nhận quyền mới ở thao tác kế tiếp',
    ROLE_DELETED = 'Đã xóa vai trò',
    DELETE_TITLE = 'Xóa vai trò',
    DELETE_CONFIRM = 'Xóa',
    SYSTEM_LOCKED_NOTICE = 'Vai trò hệ thống chỉ xem - không sửa được tên, mô tả hay ma trận quyền',
    SELF_COVERS_NOTHING = 'không phủ đơn vị nào',
    ADMIN_ONLY = 'Chỉ vai trò Quản trị hệ thống',
    NOT_GRANTED = '-',
    SYSTEM_CHIP = 'Hệ thống',
    CODE_FORMAT_HINT = 'Chỉ gồm chữ in hoa, số và dấu gạch dưới',
}

export function deleteRoleMessage(name: string): string {
    return `Xóa vai trò "${name}"? Thao tác không hoàn tác được`;
}