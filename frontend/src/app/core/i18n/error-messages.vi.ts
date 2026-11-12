import { ErrorCode } from "@api/models";

// Thiếu một mã lỗi là lỗi biên dịch - backend thêm mã thì FE buộc thêm nhãn
export const ERROR_MESSAGES_VI: Record<ErrorCode, string> = {
    [ErrorCode.VALIDATION_FAILED]: 'Dữ liệu nhập chưa hợp lệ, vui lòng kiểm tra lại',
    [ErrorCode.MALFORMED_REQUEST]: 'Yêu cầu không đúng định dạng',
    [ErrorCode.UNKNOWN_FIELD]: 'Yêu cầu chứa trường không được hỗ trợ',
    [ErrorCode.UNAUTHORIZED]: 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại',
    [ErrorCode.FORBIDDEN]: 'Bạn không có quyền thực hiện thao tác này',
    [ErrorCode.FIELD_GROUP_FORBIDDEN]: 'Bạn không có quyền sửa nhóm thông tin này',
    [ErrorCode.RESOURCE_NOT_FOUND]: 'Không tìm thấy dữ liệu yêu cầu',
    [ErrorCode.METHOD_NOT_ALLOWED]: 'Thao tác không được hỗ trợ',
    [ErrorCode.CONFLICT]: 'Dữ liệu đã được xử lý bởi thao tác khác, vui lòng tải lại',
    [ErrorCode.UNSUPPORTED_MEDIA_TYPE]: 'Định dạng dữ liệu gửi lên không được hỗ trợ',
    [ErrorCode.FILE_TOO_LARGE]: 'Tệp vượt quá dung lượng cho phép',
    [ErrorCode.UNSUPPORTED_FILE_TYPE]: 'Chỉ nhận ảnh JPEG, PNG hoặc WebP',
    [ErrorCode.PASSWORD_SAME_AS_CURRENT]: 'Mật khẩu mới phải khác mật khẩu đang dùng',
    [ErrorCode.INTERNAL_ERROR]: 'Hệ thống gặp lỗi, vui lòng thử lại sau',
    [ErrorCode.STORAGE_ERROR]: 'Không lưu được tệp, vui lòng thử lại sau',
    [ErrorCode.NOT_IMPLEMENTED]: 'Chức năng đang được xây dựng',
};

export const NETWORK_ERROR_MESSAGE_VI = 'Không kết nối được máy chủ, vui lòng kiểm tra mạng';
export const UNKNOWN_ERROR_MESSAGE_VI = 'Đã có lỗi xảy ra, vui lòng thử lại';

// Trạng thái 0 = không tới được máy chủ
export function resolveErrorMessage(status: number, code?: ErrorCode | null): string {
    if (status === 0) {
        return NETWORK_ERROR_MESSAGE_VI;
    }
    return code ? ERROR_MESSAGES_VI[code] : UNKNOWN_ERROR_MESSAGE_VI;
}