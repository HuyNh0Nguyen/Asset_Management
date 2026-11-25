import { AttributeDataType, CatalogStatus, DataScope, OrgUnitAction, OrgUnitBlockerKind, OrgUnitStatus, OrgUnitType, Permission, RoleAction, UserAction, UserBlockerKind, UserStatus } from "@api/models";

// Nhãn tiếng Việt cho enum - khai bằng Record để thiếu nhãn là lỗi biên dịch
export const ATTRIBUTE_DATA_TYPE_LABELS: Record<AttributeDataType, string> = {
    [AttributeDataType.TEXT]: 'Văn bản',
    [AttributeDataType.NUMBER]: 'Số',
    [AttributeDataType.DATE]: 'Ngày',
    [AttributeDataType.BOOLEAN]: 'Có / Không',
    [AttributeDataType.SELECT]: 'Chọn từ danh sách',
};

export const CATALOG_STATUS_LABELS: Record<CatalogStatus, string> = {
    [CatalogStatus.ACTIVE]: 'Đang dùng',
    [CatalogStatus.INACTIVE]: 'Ngừng dùng',
};

export enum YesNoLabel {
    YES = 'Có',
    NO = 'Không',
};

export const ORG_UNIT_TYPE_LABELS: Record<OrgUnitType, string> = {
    [OrgUnitType.COMPANY]: 'Công ty',
    [OrgUnitType.BRANCH]: 'Chi nhánh',
    [OrgUnitType.DEPARTMENT]: 'Phòng ban',
}

export const ORG_UNIT_STATUS_LABELS: Record<OrgUnitStatus, string> = {
    [OrgUnitStatus.ACTIVE]: 'Đang hoạt động',
    [OrgUnitStatus.INACTIVE]: 'Ngừng hoạt động',
};

export const ORG_UNIT_ACTION_LABELS: Record<OrgUnitAction, string> = {
    [OrgUnitAction.ADD_CHILD]: 'Thêm đơn vị con',
    [OrgUnitAction.EDIT]: 'Sửa',
    [OrgUnitAction.MOVE]: 'Đổi đơn vị cha',
    [OrgUnitAction.DELETE]: 'Xóa',
    [OrgUnitAction.DEACTIVATE]: 'Ngừng hoạt động',
    [OrgUnitAction.REACTIVATE]: 'Kích hoạt lại',
};

export const ORG_UNIT_BLOCKER_LABELS: Record<OrgUnitBlockerKind, string> = {
    [OrgUnitBlockerKind.ACTIVE_USER]: 'Người dùng đang hoạt động',
    [OrgUnitBlockerKind.USER]: 'Người dùng (kể cả đã nghỉ)',
    [OrgUnitBlockerKind.ACTIVE_CHILD_UNIT]: 'Đơn vị con đang hoạt động',
    [OrgUnitBlockerKind.CHILD_UNIT]: 'Đơn vị con',
    [OrgUnitBlockerKind.LIVE_ASSET]: 'Tài sản chưa thanh lý',
    [OrgUnitBlockerKind.ASSET]: 'Tài sản',
    [OrgUnitBlockerKind.LIVE_BATCH]: 'Lô tài sản chưa thanh lý hết',
    [OrgUnitBlockerKind.BATCH]: 'Lô tài sản',
};

// Nhãn ngăn của quyền - ma trận quyền, "Quyền hiệu lực của tôi"
export const PERMISSION_LABELS: Record<Permission, string> = {
    [Permission.ASSET_VIEW]: 'Xem tài sản',
    [Permission.ASSET_CREATE]: 'Nhập tài sản',
    [Permission.ASSET_TECH_EDIT]: 'Sửa thông tin kỹ thuật',
    [Permission.ASSET_FINANCE_VIEW]: 'Xem tài chính',
    [Permission.ASSET_FINANCE_EDIT]: 'Ghi nhận tài chính',
    [Permission.ASSET_OPERATE]: 'Vận hành vòng đời',
    [Permission.ASSET_TRANSFER]: 'Điều chuyển tài sản',
    [Permission.DISPOSAL_PROPOSE]: 'Đề xuất thanh lý',
    [Permission.REQUEST_CREATE]: 'Lập phiếu cho mình',
    [Permission.REQUEST_VIEW]: 'Giám sát phiếu',
    [Permission.CATALOG_MANAGE]: 'Quản lý danh mục',
    [Permission.DASHBOARD_VIEW]: 'Xem thống kê',
    [Permission.WORKFLOW_MANAGE]: 'Cấu hình quy trình duyệt',
    [Permission.TASK_REASSIGN]: 'Chuyển người duyệt',
    [Permission.ORG_MANAGE]: 'Quản lý cơ cấu tổ chức',
    [Permission.USER_MANAGE]: 'Quản lý người dùng',
    [Permission.ROLE_MANAGE]: 'Quản lý phân quyền',
    [Permission.AUDIT_VIEW]: 'Xem nhật ký hệ thống',
    [Permission.SYSTEM_CONFIG]: 'Cấu hình hệ thống',
};

export const PERMISSION_DESCRIPTIONS: Record<Permission, string> = {
    [Permission.ASSET_VIEW]: 'Xem tài sản / lô: nhóm Định danh + Kỹ thuật, lịch sử tác động',
    [Permission.ASSET_CREATE]: 'Nhập tài sản / lô vào đơn vị quản lý trong phạm vi; xóa tài sản nhập nhầm',
    [Permission.ASSET_TECH_EDIT]: 'Sửa nhóm Định danh + Kỹ thuật, kể cả ảnh tài sản',
    [Permission.ASSET_FINANCE_VIEW]: 'Xem nhóm Tài chính',
    [Permission.ASSET_FINANCE_EDIT]: 'Ghi nhận / sửa nhóm Tài chính, chi phí bảo trì, số tiền thu về khi thanh lý',
    [Permission.ASSET_OPERATE]: 'Bàn giao, thu hồi, gia hạn, bảo trì, kiểm định, thực hiện thanh lý, đóng phiếu đã duyệt',
    [Permission.ASSET_TRANSFER]: 'Điều chuyển đơn vị quản lý - phạm vi phủ cả đơn vị nguồn lẫn đích',
    [Permission.DISPOSAL_PROPOSE]: 'Lập phiếu thanh lý cho tài sản trong phạm vi',
    [Permission.REQUEST_CREATE]: 'Lập phiếu cấp phát / mượn cho chính mình',
    [Permission.REQUEST_VIEW]: 'Xem phiếu của người khác trong phạm vi',
    [Permission.CATALOG_MANAGE]: 'Loại tài sản, định nghĩa thuộc tính, nhà cung cấp',
    [Permission.DASHBOARD_VIEW]: 'Xem Dashboard - số liệu cắt trong phạm vi',
    [Permission.WORKFLOW_MANAGE]: 'Cấu hình quy trình duyệt',
    [Permission.TASK_REASSIGN]: 'Chuyển người duyệt, xử lý hàng đợi "Chưa có người duyệt"',
    [Permission.ORG_MANAGE]: 'Cây đơn vị, trưởng đơn vị, danh mục cấp bậc',
    [Permission.USER_MANAGE]: 'Tạo / sửa / vô hiệu hóa người dùng, gán vai trò, đặt lại mật khẩu',
    [Permission.ROLE_MANAGE]: 'Tạo / sửa vai trò và quyền trong vai trò',
    [Permission.AUDIT_VIEW]: 'Xem Audit Log toàn hệ thống',
    [Permission.SYSTEM_CONFIG]: 'Cấu hình cronjob, ngưỡng cảnh báo, SLA',
};

export const DATA_SCOPE_LABELS: Record<DataScope, string> = {
    [DataScope.SELF]: 'Bản thân',
    [DataScope.DEPARTMENT]: 'Phòng ban',
    [DataScope.BRANCH]: 'Chi nhánh',
    [DataScope.ALL]: 'Toàn công ty',
};

export const USER_STATUS_LABELS: Record<UserStatus, string> = {
    [UserStatus.ACTIVE]: 'Đang hoạt động',
    [UserStatus.INACTIVE]: 'Đã vô hiệu hóa',
};

export const USER_ACTION_LABELS: Record<UserAction, string> = {
    [UserAction.EDIT]: 'Sửa',
    [UserAction.RESET_PASSWORD]: 'Đặt lại mật khẩu',
    [UserAction.CHANGE_AVATAR]: 'Đổi ảnh đại diện',
    [UserAction.REMOVE_AVATAR]: 'Gỡ ảnh đại diện',
    [UserAction.DEACTIVATE]: 'Vô hiệu hóa',
    [UserAction.REACTIVATE]: 'Kích hoạt lại',
};

export const USER_BLOCKER_LABELS: Record<UserBlockerKind, string> = {
    [UserBlockerKind.LAST_ACTIVE_ADMIN]: 'Quản trị viên cuối cùng',
    [UserBlockerKind.OPEN_ALLOCATION]: 'Tài sản đang giữ',
    [UserBlockerKind.NAMED_APPROVER]: 'Được chỉ định trong quy trình duyệt',
};

export const ROLE_ACTION_LABELS: Record<RoleAction, string> = {
    [RoleAction.EDIT]: 'Sửa',
    [RoleAction.DELETE]: 'Xóa',
};