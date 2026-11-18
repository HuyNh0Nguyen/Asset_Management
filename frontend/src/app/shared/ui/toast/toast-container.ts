import { ChangeDetectionStrategy, Component, DestroyRef, effect, inject, signal, untracked } from "@angular/core";
import { Toast, ToastType } from "./toast.model";
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

// Thời gian hiệu ứng biến mất (ms) để đồng bộ với SCSS
const EXIT_ANIMATION_DURATION_MS = 250;

interface DisplayToast extends Toast {
    isLeaving?: boolean;
}

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

    protected readonly displayToasts = signal<DisplayToast[]>([]);
    private readonly leavingIds = new Set<number>();
    private readonly timeoutHandles = new Map<number, ReturnType<typeof setTimeout>>();

    constructor() {
        const destroyRef = inject(DestroyRef);
        destroyRef.onDestroy(() => {
            for (const timer of this.timeoutHandles.values()) {
                clearTimeout(timer);
            }
            this.timeoutHandles.clear();
            this.leavingIds.clear();
        });

        effect(() => {
            const serviceToasts = this.toastService.toasts();
            const serviceIds = new Set(serviceToasts.map((t) => t.id));

            untracked(() => {
                const current = this.displayToasts();
                const next: DisplayToast[] = [];

                // Giữ hoặc thêm các toast hiện có từ service
                for (const toast of serviceToasts) {
                    const existing = current.find((t) => t.id === toast.id);
                    next.push(existing ?? { ...toast });
                }

                // Với các toast đã bị loại bỏ khỏi service nhưng chưa chạy xong animation thoát:
                for (const item of current) {
                    if (!serviceIds.has(item.id)) {
                        if (!this.leavingIds.has(item.id)) {
                            this.startLeaving(item.id);
                        }
                        if (this.leavingIds.has(item.id)) {
                            next.push({ ...item, isLeaving: true });
                        }
                    }
                }

                this.displayToasts.set(next);
            });
        });
    }

    protected dismiss(id: number): void {
        if (this.leavingIds.has(id)) {
            return;
        }
        this.startLeaving(id);
        this.displayToasts.update((list) =>
            list.map((t) => (t.id === id ? { ...t, isLeaving: true } : t))
        );
        this.toastService.dismiss(id);
    }

    private startLeaving(id: number): void {
        if (this.leavingIds.has(id)) {
            return;
        }
        this.leavingIds.add(id);

        const timer = setTimeout(() => {
            this.leavingIds.delete(id);
            this.timeoutHandles.delete(id);
            this.displayToasts.update((list) => list.filter((t) => t.id !== id));
        }, EXIT_ANIMATION_DURATION_MS);

        this.timeoutHandles.set(id, timer);
    }
}
