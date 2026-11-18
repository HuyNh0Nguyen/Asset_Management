import { ChangeDetectionStrategy, Component, inject, ViewContainerRef } from "@angular/core";
import { MatButton, MatIconButton } from "@angular/material/button";
import { MatDialog } from "@angular/material/dialog";
import { MatIcon } from "@angular/material/icon";
import { MatProgressSpinner } from "@angular/material/progress-spinner";
import {
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderCellDef,
    MatHeaderRow,
    MatHeaderRowDef,
    MatRow,
    MatRowDef,
    MatTable,
} from "@angular/material/table";
import { MatTooltip } from "@angular/material/tooltip";
import { RankResponse } from "@api/models";
import { RankFormDialog } from "../../components/rank-form-dialog/rank-form-dialog";
import { RankFormDialogData } from "../../components/rank-form-dialog/rank-form-dialog.model";
import { RanksStore } from "../../data/ranks.store";
import { OrgMessage } from "../../organization-messages";
import { confirmAction } from "@shared/ui/confirm-dialog/confirm-dialog";
import { ConfirmTone } from "@shared/ui/confirm-dialog/confirm-dialog.model";
import { appDialogConfig, DialogWidth } from "@shared/ui/dialog/app-dialog-config";
import { ToastService } from "@shared/ui/toast/toast.service";
import { filter, switchMap } from "rxjs";

// Cột của bảng cấp bậc
enum RankColumn {
    CODE = 'code',
    NAME = 'name',
    SENIORITY = 'seniority',
    ACTIONS = 'actions',
}

// Cấp bậc sắp theo số thứ bậc; không kéo thả (đổi chỗ phải qua số trung gian)
@Component({
    selector: 'app-rank-list-page',
    imports: [
        MatButton,
        MatIconButton,
        MatIcon,
        MatTooltip,
        MatProgressSpinner,
        MatTable,
        MatColumnDef,
        MatHeaderCell,
        MatHeaderCellDef,
        MatCell,
        MatCellDef,
        MatHeaderRow,
        MatHeaderRowDef,
        MatRow,
        MatRowDef,
    ],
    templateUrl: './rank-list-page.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RankListPage {

    private readonly store = inject(RanksStore);
    private readonly dialog = inject(MatDialog);
    private readonly toast = inject(ToastService);
    private readonly viewContainerRef = inject(ViewContainerRef);

    protected readonly ranks = this.store.ranks;
    protected readonly loading = this.store.loading;
    protected readonly loaded = this.store.loaded;
    protected readonly column = RankColumn;
    protected readonly columns: RankColumn[] = [RankColumn.CODE, RankColumn.NAME, RankColumn.SENIORITY, RankColumn.ACTIONS];

    constructor() {
        this.store.load();
    }

    protected openCreate(): void {
        this.openForm({});
    }

    protected openEdit(rank: RankResponse): void {
        this.openForm({ rank });
    }

    // 422 RANK_IN_USE để interceptor toast
    protected remove(rank: RankResponse): void {
        confirmAction(this.dialog, {
            title: 'Xóa cấp bậc',
            message: `Xóa cấp bậc "${rank.name}" (${rank.code})? Cấp bậc đang được người dùng hoặc quy trình dùng thì chỉ đổi tên được`,
            confirmLabel: 'Xóa',
            tone: ConfirmTone.DANGER,
        }, this.viewContainerRef)
            .pipe(
                filter(Boolean),
                switchMap(() => this.store.delete(rank.id)),
            )
            .subscribe({
                next: () => {
                    this.toast.success(OrgMessage.RANK_DELETED);
                    this.store.load();
                },
                error: () => this.store.load(),
            });
    }

    private openForm(data: RankFormDialogData): void {
        this.dialog
            .open<RankFormDialog, RankFormDialogData, boolean>(RankFormDialog,
                appDialogConfig(data, DialogWidth.NARROW, this.viewContainerRef)
            )
            .afterClosed()
            .subscribe((changed) => {
                if (changed) {
                    this.store.load();
                }
            });
    }
}