import { inject, Injectable, signal } from "@angular/core";
import { EmailProbeRequest, StorageProbeResponse } from "@api/models";
import { DevToolsService } from "@api/services";
import { ToastService } from "@shared/ui/toast/toast.service";
import { finalize } from "rxjs";

// Lỗi HTTP đã được error.interceptor hiện toast - ở đây chỉ nuốt để không log trùng
const ignoreError = (): void => undefined;

@Injectable()
export class ProbesStore {
    
    private readonly devTools = inject(DevToolsService);
    private readonly toast = inject(ToastService);

    readonly storageProbe = signal<StorageProbeResponse | null>(null);
    readonly uploading = signal(false);
    readonly sendingEmail = signal(false);

    uploadImage(file: File): void {
        this.uploading.set(true);
        this.devTools
            .uploadStorageProbe({ body: { file }})
            .pipe(finalize(() => this.uploading.set(false)))
            .subscribe({
                next: (probe) => {
                    this.storageProbe.set(probe);
                    this.toast.success('Tải ảnh lên thành công');
                },
                error: ignoreError,
            });
    }

    refreshUrl(): void {
        const current = this.storageProbe();
        if (!current) {
            return;
        }
        this.devTools.refreshStorageProbeUrl({ probeId: current.probeId }).subscribe({
            next: (probe) => {
                this.storageProbe.set(probe);
                this.toast.info('Đã cấp URL ký mới');
            },
            error: ignoreError,
        });
    }

    sendEmail(request: EmailProbeRequest): void {
        this.sendingEmail.set(true);
        this.devTools
            .sendEmailProbe({ body: request })
            .pipe(finalize(() => this.sendingEmail.set(false)))
            .subscribe({
                next: () => this.toast.success('Đã đưa email thử vào hàng đợi — kiểm tra hộp thư Gmail của người nhận'),
                error: ignoreError,
            });
    }
}