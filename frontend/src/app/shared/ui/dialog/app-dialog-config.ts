import { ViewContainerRef } from "@angular/core";
import { MatDialogConfig } from "@angular/material/dialog";

// Bề rộng hộp thoại trên màn hình lớn; điện thoại luôn toàn màn hình (_dialog.scss)
export enum DialogWidth {
    NARROW = '440px',
    MEDIUM = '560px',
    WIDE = '680px',
}

export const APP_DIALOG_PANEL_CLASS = 'app-dialog-panel';

// viewContainerRef: hộp thoại dùng được provider của route (store) qua cây injector của trang
export function appDialogConfig<D>(data: D, width: DialogWidth, viewContainerRef?: ViewContainerRef): MatDialogConfig<D> {
    return {
        data,
        width,
        maxWidth: '100vw',
        panelClass: APP_DIALOG_PANEL_CLASS,
        autoFocus: 'first-tabbable',
        restoreFocus: true,
        viewContainerRef,
    };
}