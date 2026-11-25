import { Observable } from "rxjs";

// Hộp thoại không biết miền: bên mở truyền hàm tải lên (ảnh của tôi / của người khác)
export interface AvatarUploadDialogData {
    title: string;
    upload: (file: File) => Observable<unknown>;
}

export enum AvatarUploadText {
    UNSUPPORTED = 'Chỉ nhận ảnh JPEG, PNG hoặc WebP',
    TOO_LARGE = 'Ảnh vượt quá 5 MB',
    UNREADABLE = 'Không đọc được ảnh, vui lòng chọn tập khác',
    FILE_NAME = 'avatar.jpg',
}