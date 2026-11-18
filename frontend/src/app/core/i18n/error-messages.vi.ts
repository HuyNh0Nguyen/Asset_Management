import { ErrorCode } from "@api/models";

// Thiếu một mã lỗi là lỗi biên dịch - backend thêm mã thì FE buộc thêm nhãn
export const ERROR_MESSAGES_VI: Record<ErrorCode, string> = {
    [ErrorCode.VALIDATION_FAILED]: 'Dữ liệu nhập chưa hợp lệ, vui lòng kiểm tra lại',
    [ErrorCode.MALFORMED_REQUEST]: 'Yêu cầu không đúng định dạng',
    [ErrorCode.UNKNOWN_FIELD]: 'Yêu cầu chứa trường không được hỗ trợ',
    [ErrorCode.UNAUTHORIZED]: 'Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại',
    [ErrorCode.INVALID_CREDENTIALS]: 'Tên đăng nhập hoặc mật khẩu không chính xác',
    [ErrorCode.ACCOUNT_LOCKED]: 'Tài khoản tạm khóa do đăng nhập sai nhiều lần, vui lòng thử lại sau 5 phút',
    [ErrorCode.ACCOUNT_DISABLED]: 'Tài khoản đã bị vô hiệu hóa, vui lòng liên hệ quản trị viên',
    [ErrorCode.SESSION_EXPIRED]: 'Phiên đăng nhập đã kết thúc, vui lòng đăng nhập lại',
    [ErrorCode.FORBIDDEN]: 'Bạn không có quyền thực hiện thao tác này',
    [ErrorCode.FIELD_GROUP_FORBIDDEN]: 'Bạn không có quyền sửa nhóm thông tin này',
    [ErrorCode.PASSWORD_CHANGE_REQUIRED]: 'Bạn cần đổi mật khẩu trước khi tiếp tục',
    [ErrorCode.RESOURCE_NOT_FOUND]: 'Không tìm thấy dữ liệu yêu cầu',
    [ErrorCode.METHOD_NOT_ALLOWED]: 'Thao tác không được hỗ trợ',
    [ErrorCode.CONFLICT]: 'Dữ liệu đã được xử lý bởi thao tác khác, vui lòng tải lại',
    [ErrorCode.UNSUPPORTED_MEDIA_TYPE]: 'Định dạng dữ liệu gửi lên không được hỗ trợ',
    [ErrorCode.FILE_TOO_LARGE]: 'Tệp vượt quá dung lượng cho phép',
    [ErrorCode.UNSUPPORTED_FILE_TYPE]: 'Chỉ nhận ảnh JPEG, PNG hoặc WebP',
    [ErrorCode.PASSWORD_SAME_AS_CURRENT]: 'Mật khẩu mới phải khác mật khẩu đang dùng',
    [ErrorCode.USER_NOT_ACTIVE]: 'Chỉ đặt lại được mật khẩu cho tài khoản đang hoạt động',
    [ErrorCode.INTERNAL_ERROR]: 'Hệ thống gặp lỗi, vui lòng thử lại sau',
    [ErrorCode.STORAGE_ERROR]: 'Không lưu được tệp, vui lòng thử lại sau',
    [ErrorCode.NOT_IMPLEMENTED]: 'Chức năng đang được xây dựng',
    [ErrorCode.ORG_UNIT_CODE_DUPLICATE]: 'Mã đơn vị đã tồn tại',
    [ErrorCode.RANK_CODE_DUPLICATE]: 'Mã cấp bậc đã tồn tại',
    [ErrorCode.RANK_SENIORITY_DUPLICATE]: 'Số thứ bậc đã được dùng cho cấp bậc khác',
    [ErrorCode.ORG_UNIT_COMPANY_NOT_ALLOWED]: 'Không thể tạo thêm đơn vị cấp Công ty',
    [ErrorCode.ORG_UNIT_INVALID_PARENT_TYPE]: 'Đơn vị cha không phù hợp với loại đơn vị',
    [ErrorCode.ORG_UNIT_PARENT_CYCLE]: 'Không thể chuyển đơn vị vào chính nó hoặc đơn vị con của nó',
    [ErrorCode.ORG_UNIT_PARENT_INACTIVE]: 'Đơn vị cha đang ngừng hoạt động',
    [ErrorCode.ORG_UNIT_ROOT_IMMUTABLE]: 'Không thể thực hiện thao tác này với đơn vị gốc',
    [ErrorCode.ORG_UNIT_HEAD_NOT_ACTIVE]: 'Trưởng đơn vị phải là người dùng đang hoạt động',
    [ErrorCode.ORG_UNIT_HEAD_OUTSIDE_SUBTREE]: 'Trưởng đơn vị phải thuộc đơn vị này hoặc một đơn vị con',
    [ErrorCode.ORG_UNIT_MOVE_DETACHES_HEAD]: 'Đơn vị này đang chứa trưởng của đơn vị cấp trên - hãy chỉ định trưởng khác cho đơn vị đó trước khi chuyển',
    [ErrorCode.ORG_UNIT_NOT_EMPTY]: 'Đơn vị còn người dùng, đơn vị con hoặc tài sản đang hoạt động',
    [ErrorCode.ORG_UNIT_IN_USE]: 'Đơn vị còn dữ liệu tham chiếu nên không thể xóa',
    [ErrorCode.ORG_UNIT_INACTIVE]: 'Đơn vị đã ngừng hoạt động, vui lòng chọn đơn vị khác',
    [ErrorCode.ORG_UNIT_TYPE_NOT_ALLOWED]: 'Loại đơn vị không phù hợp',
    [ErrorCode.RANK_IN_USE]: 'Cấp bậc đang được sử dụng, chỉ có thể đổi tên',
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