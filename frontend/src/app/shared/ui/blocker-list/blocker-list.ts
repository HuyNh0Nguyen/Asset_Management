import { ChangeDetectionStrategy, Component, input } from "@angular/core";
import { StatusChip } from "../status-chip/status-chip";
import { BlockerGroup } from "./blocker-group.model";
import { StatusTone } from "../status-chip/status-tone";

// Danh sách vướng theo nhóm; total > số mẫu thì hiện "và N mục khác"
@Component({
    selector: 'app-blocker-list',
    imports: [StatusChip],
    templateUrl: './blocker-list.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BlockerList {

    readonly groups = input.required<BlockerGroup[]>();
    readonly inactiveLabel = input('Đã ngừng');

    protected readonly mutedTone = StatusTone.MUTED;
}