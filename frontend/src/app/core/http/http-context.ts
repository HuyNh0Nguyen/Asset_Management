import { HttpContext, HttpContextToken } from "@angular/common/http";

// Đặt true khi màn hình tự xử lý lỗi, không muốn toast mặc định
export const SKIP_ERROR_TOAST = new HttpContextToken<boolean>(() => false);

export function skipErrorToast(): HttpContext {
    return new HttpContext().set(SKIP_ERROR_TOAST, true);
}