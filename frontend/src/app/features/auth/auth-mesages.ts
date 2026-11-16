// Câu thông báo của miền xác thực
export enum AuthMessage {
    FORGOT_PASSWORD_HINT = 'Vui lòng liên hệ quản trị viên để được đặt lại mật khẩu',
    ACCOUNT_LOCKED_RETRY = 'Tài khoản tạm khóa do đăng nhập sai nhiều lần. Vui lòng thử lại sau',
    PASSWORD_CHANGED = 'Đổi mật khẩu thành công, vui lòng đăng nhập lại',
    MANDATORY_CHANGE_NOTICE = 'Bạn cần đổi mật khẩu trước khi tiếp tục sử dụng hệ thống',
}