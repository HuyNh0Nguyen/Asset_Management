import { OrgUnitBlockerKind } from "@api/models";

// Câu thông báo của miền tổ chức
export enum OrgMessage {
    UNIT_CREATED = 'Đã thêm đơn vị',
    UNIT_UPDATED = 'Đã cập nhật đơn vị',
    UNIT_MOVED = 'Đã chuyển đơn vị',
    UNIT_DELETED = 'Đã xóa đơn vị',
    UNIT_DEACTIVATED = 'Đã ngừng hoạt động đơn vị',
    UNIT_REACTIVATED = 'Đã kích hoạt lại đơn vị',
    RANK_CREATED = 'Đã thêm cấp bậc',
    RANK_UPDATED = 'Đã cập nhật cấp bậc',
    RANK_DELETED = 'Đã xóa cấp bậc',
    CODE_FORMAT_HINT = 'Chỉ gồm chữ in hoa, số và dấu gạch dưới',
    NO_HEAD = 'Chưa có trưởng',
    NO_HEAD_OPTION = '- Để trống -',
    USE_DEACTIVATE_INSTEAD = 'Dùng Ngừng hoạt động thay cho Xóa: đơn vị vẫn hiện trên cây và trong tra cứu, nhưng vắng khỏi mọi bộ chọn khi ghi',
    REACTIVATE_NOTICE = 'Đơn vị trở lại không có người dùng và chưa có trưởng - gán lại qua Người dùng và Sửa',
}

// Gợi ý cách gỡ từng nhóm vướng
export const ORG_UNIT_BLOCKER_HINTS: Record<OrgUnitBlockerKind, string> = {
    [OrgUnitBlockerKind.ACTIVE_USER]: 'Chuyển sang đơn vị khác hoặc vô hiệu hóa tài khoản',
    [OrgUnitBlockerKind.USER]: 'Người dùng không bao giờ bị xóa - dùng Ngừng hoạt động',
    [OrgUnitBlockerKind.ACTIVE_CHILD_UNIT]: 'Ngừng hoạt động đơn vị con trước (từ dưới lên)',
    [OrgUnitBlockerKind.CHILD_UNIT]: 'Xóa hoặc chuyển đơn vị con trước',
    [OrgUnitBlockerKind.LIVE_ASSET]: 'Thu hồi, điều chuyển hoặc thanh lý',
    [OrgUnitBlockerKind.ASSET]: 'Tài sản giữ lịch sử của đơn vị - dùng Ngừng hoạt động',
    [OrgUnitBlockerKind.LIVE_BATCH]: 'Thu hồi, điều chuyển hoặc thanh lý',
    [OrgUnitBlockerKind.BATCH]: 'Lô tài sản giữ lịch sử của đơn vị - dùng Ngừng hoạt động',
};