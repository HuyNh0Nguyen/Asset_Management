import { UserBlockerItemResponse, UserBlockerKind, UserBlockerResponse } from "@api/models";
import { USER_BLOCKER_HINTS, UserMessage } from "../user-messages";
import { BlockerGroup } from "@shared/ui/blocker-list/blocker-group.model";
import { USER_BLOCKER_LABELS } from "@core/i18n/labels.vi";

// Dòng phụ: mã + số lượng đang giữ / tên bước duyệt
function secondaryOf(kind: UserBlockerKind, sample: UserBlockerItemResponse): string {
    if (!sample.detail) {
        return sample.code;
    }
    switch (kind) {
        case UserBlockerKind.OPEN_ALLOCATION:
            return `${sample.code} · ${UserMessage.QUANTITY_PREFIX} ${sample.detail}`;
        case UserBlockerKind.NAMED_APPROVER:
            return `${sample.code} · ${UserMessage.STAGE_PREFIX}: ${sample.detail}`;
        default:
            return sample.code;
    }
}

// Nhóm chặn vô hiệu hóa -> nhóm hiển thị chung của blocker-list
export function toUserBlockerGroup(blocker: UserBlockerResponse): BlockerGroup {
    // LAST_ACTIVE_ADMIN không có mẫu - tự dựng một dòng để không hiện "và 1 mục khác"
    const items = blocker.kind === UserBlockerKind.LAST_ACTIVE_ADMIN
        ? [{ key: blocker.kind, primary: UserMessage.LAST_ADMIN_ITEM, inactive: false }]
        : blocker.samples.map((sample) => ({
            key: sample.id,
            primary: sample.name,
            secondary: secondaryOf(blocker.kind, sample),
            inactive: false,
        }));
    return {
        key: blocker.kind,
        label: USER_BLOCKER_LABELS[blocker.kind],
        hint: USER_BLOCKER_HINTS[blocker.kind],
        total: blocker.total,
        items,
    };
}