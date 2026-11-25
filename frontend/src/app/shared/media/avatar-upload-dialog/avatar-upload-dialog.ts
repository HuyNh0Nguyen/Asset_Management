import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal, viewChild, ViewContainerRef } from "@angular/core";
import { MatButton } from "@angular/material/button";
import { MAT_DIALOG_DATA, MatDialog, MatDialogActions, MatDialogClose, MatDialogContent, MatDialogRef, MatDialogTitle } from "@angular/material/dialog";
import { MatIcon } from "@angular/material/icon";
import { ErrorCode } from "@api/models";
import { ImageCropper } from "../image-cropper/image-cropper";
import { ToastService } from "@shared/ui/toast/toast.service";
import { AvatarUploadDialogData, AvatarUploadText } from "./avatar-upload-dialog.model";
import { ImageMimeType, MAX_IMAGE_BYTES } from "../avatar-size";
import { finalize, map, Observable } from "rxjs";
import { parseHttpError } from "../../../core/http/parse-http-error";
import { ERROR_MESSAGES_VI } from "@core/i18n/error-messages.vi";
import { describeHttpError } from "../../../core/http/http-error-message";
import { appDialogConfig, DialogWidth } from "@shared/ui/dialog/app-dialog-config";

// Lỗi tệp hiện ngay trong hộp thoại; lỗi khác toast
const FILE_ERRORS = new Set<ErrorCode>([
    ErrorCode.UNSUPPORTED_FILE_TYPE,
    ErrorCode.FILE_TOO_LARGE,
    ErrorCode.IMAGE_DIMENSIONS_TOO_LARGE,
]);

// Chọn tệp -> kiểm sớm phía client -> cắt vuông -> tải lên; đóng với true khi lưu xong
@Component({
    selector: 'app-avatar-upload-dialog',
    imports: [MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose, MatButton, MatIcon, ImageCropper],
    templateUrl: './avatar-upload-dialog.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AvatarUploadDialog {

    private readonly dialogRef = inject<MatDialogRef<AvatarUploadDialog, boolean>>(MatDialogRef);
    private readonly toast = inject(ToastService);
    protected readonly data = inject<AvatarUploadDialogData>(MAT_DIALOG_DATA);

    protected readonly accept = Object.values(ImageMimeType).join(',');
    protected readonly bitmap = signal<ImageBitmap | null>(null);
    protected readonly error = signal<string | null>(null);
    protected readonly uploading = signal(false);
    private readonly cropper = viewChild(ImageCropper);

    constructor() {
        inject(DestroyRef).onDestroy(() => this.bitmap()?.close());
    }

    // Kiểm kiểu / dung lượng chỉ để báo sớm - máy chủ là chuẩn
    protected async onFileSelected(event: Event): Promise<void> {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        input.value = '';
        if (!file) {
            return;
        }
        if (!(Object.values(ImageMimeType) as string[]).includes(file.type)) {
            this.error.set(AvatarUploadText.UNSUPPORTED);
            return;
        }
        if (file.size > MAX_IMAGE_BYTES) {
            this.error.set(AvatarUploadText.TOO_LARGE);
            return;
        }
        try {
            // Trình duyệt tự xoay theo EXIF
            const bitmap = await createImageBitmap(file);
            this.bitmap()?.close();
            this.bitmap.set(bitmap);
            this.error.set(null);
        } catch {
            this.error.set(AvatarUploadText.UNREADABLE);
        }
    }

    protected async save(): Promise<void> {
        const cropper = this.cropper();
        if (!cropper || this.uploading()) {
            return;
        }
        this.uploading.set(true);
        let blob: Blob;
        try {
            blob = await cropper.exportBlob();
        } catch {
            this.uploading.set(false);
            this.error.set(AvatarUploadText.UNREADABLE);
            return;
        }
        const file = new File([blob], AvatarUploadText.FILE_NAME, { type: ImageMimeType.JPEG });
        this.data.upload(file)
            .pipe(finalize(() => this.uploading.set(false)))
            .subscribe({
                next: () => this.dialogRef.close(true),
                error: (error: unknown) => this.onFailed(error),
            });
    }

    private onFailed(error: unknown): void {
        const { code } = parseHttpError(error);
        if (code && FILE_ERRORS.has(code)) {
            this.error.set(ERROR_MESSAGES_VI[code]);
            return;
        }
        this.toast.error(describeHttpError(error));
    }
}

// Mở hộp thoại, phát truy khi đã lưu ảnh
export function openAvatarUpload(dialog: MatDialog, data: AvatarUploadDialogData,
                                viewContainerRef?: ViewContainerRef): Observable<boolean> {
    return dialog
        .open<AvatarUploadDialog, AvatarUploadDialogData, boolean>(AvatarUploadDialog,
            appDialogConfig(data, DialogWidth.NARROW, viewContainerRef)
        )
        .afterClosed()
        .pipe(map((saved) => saved === true));
}