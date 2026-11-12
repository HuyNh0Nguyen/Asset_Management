// Loại toast - giá trị chuỗi để dễ đọc khi gỡ lỗi
export enum ToastType {
    SUCCESS = 'SUCCESS',
    ERROR = 'ERROR',
    WARNING = 'WARNING',
    INFO = 'INFO',
}

export interface Toast {
    id: number;
    type: ToastType;
    message: string;
}