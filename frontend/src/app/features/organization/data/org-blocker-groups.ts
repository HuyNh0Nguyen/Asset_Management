import { OrgUnitBlockerResponse } from "@api/models";
import { ORG_UNIT_BLOCKER_LABELS } from "@core/i18n/labels.vi";
import { BlockerGroup } from "@shared/ui/blocker-list/blocker-group.model";
import { ORG_UNIT_BLOCKER_HINTS } from "../organization-messages";

// Nhóm vướng của đơn vị -> nhóm hiển thị chung; người dùng: name = họ tên, code = username
export function toBlockerGroup(blocker: OrgUnitBlockerResponse): BlockerGroup {
    return {
        key: blocker.kind,
        label: ORG_UNIT_BLOCKER_LABELS[blocker.kind],
        hint: ORG_UNIT_BLOCKER_HINTS[blocker.kind],
        total: blocker.total,
        items: blocker.samples.map((sample) => ({
            key: sample.id,
            primary: sample.name,
            secondary: sample.code,
            inactive: sample.inactive,
        })),
    };
}