import { DatePipe } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject, signal } from "@angular/core";
import { FormBuilder, ReactiveFormsModule, Validators } from "@angular/forms";
import { MatButton } from "@angular/material/button";
import { MatError, MatFormField, MatLabel } from "@angular/material/form-field";
import { MatIcon } from "@angular/material/icon";
import { MatInput } from "@angular/material/input";
import { ProbesStore } from "@features/dev-tools/data/probes.store";

// Kiểu ảnh backend chấp nhận ở endpont thử
const ACCEPTED_IMAGE_TYPES = 'image/jpeg,image/png,image/webp';

@Component({
    selector: 'app-probes-page',
    imports: [ReactiveFormsModule, DatePipe, MatButton, MatIcon, MatFormField, MatLabel, MatError, MatInput],
    providers: [ProbesStore],
    templateUrl: './probes.html',
    styleUrl: './probes.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProbesPage {
    
    protected readonly store = inject(ProbesStore);
    protected readonly acceptedImageTypes = ACCEPTED_IMAGE_TYPES;
    protected readonly selectedFile = signal<File | null>(null);

    protected readonly emailForm = inject(FormBuilder).nonNullable.group({
        recipientEmail: ['', [Validators.required, Validators.email]],
        recipientName: ['', Validators.required],
    });

    protected onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        this.selectedFile.set(input.files?.item(0) ?? null);
    }

    protected upload(): void {
        const file = this.selectedFile();
        if (file) {
            this.store.uploadImage(file);
        }
    }

    protected sendEmail(): void {
        if (this.emailForm.invalid) {
            this.emailForm.markAllAsTouched();
            return;
        }
        this.store.sendEmail(this.emailForm.getRawValue());
    }
}