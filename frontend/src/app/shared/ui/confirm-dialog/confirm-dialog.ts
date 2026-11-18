import { ChangeDetectionStrategy, Component, inject, ViewContainerRef } from "@angular/core";
import { MatButton } from "@angular/material/button";
import { MAT_DIALOG_DATA, MatDialog, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogTitle } from "@angular/material/dialog";
import { ConfirmDialogData, ConfirmTone } from "./confirm-dialog.model";
import { map, Observable } from "rxjs";
import { appDialogConfig, DialogWidth } from "../dialog/app-dialog-config";

// Hộp thoại xác nhận chung - đóng với true khi người dùng xác nhận
@Component({
    selector: 'app-confirm-dialog',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButton],
    templateUrl: './confirm-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ConfirmDialog {

    protected readonly data = inject<ConfirmDialogData>(MAT_DIALOG_DATA);
    protected readonly isDanger = this.data.tone === ConfirmTone.DANGER;
}

// Mở hộp thoại xác nhận, phát true / false
export function confirmAction(dialog: MatDialog, data: ConfirmDialogData, viewContainerRef?: ViewContainerRef): Observable<boolean> {
    return dialog
        .open<ConfirmDialog, ConfirmDialogData, boolean>(ConfirmDialog, appDialogConfig(data, DialogWidth.NARROW, viewContainerRef))
        .afterClosed()
        .pipe(map((confirmed) => confirmed === true));
}