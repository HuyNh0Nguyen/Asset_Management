// Mốc màn hình: từ 1280px trở lên menu luôn mở, nhỏ hơn (iPad, điện thoại) menu trượt
export enum LayoutBreakpoint {
    DESKTOP = '(min-width: 1280px)',
}

// Ảnh nhận diện trong thư mục public/ (đường dẫn tương đối theo <base href="/">)
export enum BrandImage {
    LOGO = 'images/logo.png',
}

// Ngưỡng responsive - khớp media query trong styles
export enum ViewportQuery {
    PHONE = '(max-width: 767.98px)',
    TABLET = '(max-width: 1279.98px)',
}

export enum ShellMessage {
    PERMISSION_CHANGED = 'Quyền của bạn vừa thay đổi - đã chuyển về trang chủ',
}