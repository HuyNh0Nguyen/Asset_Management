import { ErrorCode, FieldErrorDetail } from "@api/models";

// Lỗi HTTP đã bóc phong bì - dùng khi màn hình tự xử lý lỗi (skipErrorToast)
export interface ParsedHttpError {
    status: number;
    code: ErrorCode | null;
    // Chỉ có ở 400 VALIDATION_FAILED do @Valid
    fieldErrors: FieldErrorDetail[];
}