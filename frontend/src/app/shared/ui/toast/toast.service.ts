import { Injectable, signal } from "@angular/core";
import { Toast, ToastType } from "./toast.model";

// Thời gian hiển thị theo loại (ms) - lỗi giữ lâu hơn để kịp đọc
const TOAST_DURATION_MS: Record<ToastType, number> = {
    [ToastType.SUCCESS]: 3000,
    [ToastType.INFO]: 4000,
    [ToastType.WARNING]: 5000,
    [ToastType.ERROR]: 6000,
};

// Tối đa số toast cùng lúc, cái cũ nhất bị đẩy ra
const MAX_VISIBLE_TOASTS = 4;

@Injectable({ providedIn: 'root' })
export class ToastService {

    private readonly items = signal<Toast[]>([]);
    private nextId = 0;

    readonly toasts = this.items.asReadonly();

    success(message: string): void {
        this.show(ToastType.SUCCESS, message);
    }

    error(message: string): void {
        this.show(ToastType.ERROR, message);
    }

    warning(message: string): void {
        this.show(ToastType.WARNING, message);
    }

    info(message: string): void {
        this.show(ToastType.INFO, message);
    }

    dismiss(id: number): void {
        this.items.update((list) => list.filter((toast) => toast.id !== id));
    }

    private show(type: ToastType, message: string): void {
        const id = ++this.nextId;
        this.items.update((list) => [...list, { id, type, message }].slice(-MAX_VISIBLE_TOASTS));
        setTimeout(() => this.dismiss(id), TOAST_DURATION_MS[type]);
    }
}