import { UserBlockerKind } from "@api/models";

// Câu thông báo của miền người dùng
export enum UserMessage {
    USER_UPDATED = 'Đã cập nhật người dùng',
    USER_DEACTIVATED = 'Đã vô hiệu hóa tài khoản',
    USER_REACTIVATED = 'Đã kích hoạt lại tài khoản',
    PASSWORD_RESET = 'Đã đặt lại mật khẩu, mật khẩu tạm đã gửi tới email của người dùng',
    AVATAR_CHANGED = 'Đã đổi ảnh đại diện',
    AVATAR_REMOVED = 'Đã gỡ ảnh đại diện',
    NO_HEAD_OPTION = '- Để trống -',
    INCLUDING_YOURS = ', kể cả phiên của bạn',
    RESET_PASSWORD_TITLE = 'Đặt lại mật khẩu',
    RESET_PASSWORD_NOTICE = 'Hệ thống sinh mật khẩu tạm mới và gửi qua email. Mọi phiên đang đăng nhập của người này sẽ bị thoát',
    RESET_PASSWORD_CONFIRM = 'Đặt lại',
    CHANGE_AVATAR_TITLE = 'Đổi ảnh đại diện',
    REMOVE_AVATAR_TITLE = 'Gỡ ảnh đại diện',
    REMOVE_AVATAR_NOTICE = 'Ảnh đại diện hiện tại sẽ bị xóa, tài khoản hiển thị chữ cái đầu của tên',
    REMOVE_AVATAR_CONFIRM = 'Gỡ ảnh',
    DEACTIVATE_NOTICE = 'Tài khoản sẽ bị khóa và mọi phiên đăng nhập bị thoát',
    SELF_ADMIN_TITLE = 'Gỡ vai trò quản trị của chính bạn',
    SELF_ADMIN_NOTICE = 'Bạn sẽ mất quyền quản trị từ thao tác kế tiếp và không tự gán lại được',
    SELF_ADMIN_CONFIRM = 'Vẫn lưu',
    LAST_ADMIN_ITEM = 'Là quản trị viên đang hoạt động duy nhất',
    QUANTITY_PREFIX = 'SL',
    STAGE_PREFIX = 'Bước',
    UNIT_CHECK_LOADING = 'Đang kiểm tra các đơn vị người này làm trưởng...',
    REACTIVATE_NOTE = 'Người dùng phải đổi mật khẩu ở lần đăng nhập kế tiếp - dùng Đặt lại mật khẩu nếu họ không nhớ mật khẩu cũ',
}

export function userCreatedMessage(email: string): string {
    return `Đã tạo tài khoản, thông tin đăng nhập đã gửi tới ${email}`;
}

// "... sẽ bị thoát" + ", kể cả phiên của bạn" khi thao tác trên chính mình
export function withSessionNotice(base: string, self: boolean): string {
    return `${base}${self ? UserMessage.INCLUDING_YOURS : ''}`;
}

export function oldUnitInactiveMessage(unitName: string): string {
    return `Đơn vị cũ ${unitName} đã ngừng hoạt động, vui lòng chọn đơn vị mới`;
}

// Gợi ý cách gỡ từng nhóm chặn vô hiệu hóa
export const USER_BLOCKER_HINTS: Record<UserBlockerKind, string> = {
    [UserBlockerKind.LAST_ACTIVE_ADMIN]: 'Gán vai trò Quản trị hệ thống cho người khác trước',
    [UserBlockerKind.OPEN_ALLOCATION]: 'Quản lý tài sản thu hồi hết tài sản trước',
    [UserBlockerKind.NAMED_APPROVER]: 'Sửa các quy trình duyệt trước',
};