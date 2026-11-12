import { ChangeDetectionStrategy, Component, inject } from "@angular/core";
import { ToastType } from "./toast.model";
import { MatIcon } from "@angular/material/icon";
import { MatIconButton } from "@angular/material/button";
import { ToastService } from "./toast.service";

// Lớp CSS và biểu tượng theo loại toast
const TOAST_CLASS: Record<ToastType, string> = {
    [ToastType.SUCCESS]: 'toast-success',
    [ToastType.ERROR]: 'toast-error',
    [ToastType.WARNING]: 'toast-warning',
    [ToastType.INFO]: 'toast-info',
};

const TOAST_ICON: Record<ToastType, string> = {
    [ToastType.SUCCESS]: 'check_circle',
    [ToastType.ERROR]: 'error',
    [ToastType.WARNING]: 'warning',
    [ToastType.INFO]: 'info',
};

@Component({
    selector: 'app-toast-container',
    imports: [MatIcon, MatIconButton],
    templateUrl: './toast-container.html',
    styleUrl: './toast-container.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ToastContainer {
    
    protected readonly toastService = inject(ToastService);
    protected readonly toastClass = TOAST_CLASS;
    protected readonly toastIcon = TOAST_ICON;
    protected readonly ToastType = ToastType;
}