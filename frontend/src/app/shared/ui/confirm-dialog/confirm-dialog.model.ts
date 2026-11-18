// Kiểu nút xác nhận: DANGER cho thao tác khó hoàn tác
export enum ConfirmTone {
    PRIMARY = 'PRIMARY',
    DANGER = 'DANGER',
}

export interface ConfirmDialogData {
    title: string;
    message: string;
    confirmLabel: string;
    cancelLabel?: string;
    tone?: ConfirmTone;
}