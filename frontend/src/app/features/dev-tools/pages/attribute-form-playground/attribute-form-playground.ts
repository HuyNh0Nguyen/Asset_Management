import { JsonPipe } from "@angular/common";
import { ChangeDetectionStrategy, Component, inject, signal } from "@angular/core";
import { FormGroup } from "@angular/forms";
import { MatButton } from "@angular/material/button";
import { MatSlideToggle } from "@angular/material/slide-toggle";
import { LAPTOP_ATTRIBUTE_DEFINITIONS, LAPTOP_ATTRIBUTE_VALUES } from "../../data/laptop-attributes.mock";
import { buildAttributesPayload } from "@shared/forms/dynamic-attribute-form/attribute-form.utils";
import { AttributeValues } from "@shared/forms/dynamic-attribute-form/attribute-values.model";
import { DynamicAttributeForm } from "@shared/forms/dynamic-attribute-form/dynamic-attribute-form";
import { ToastService } from "@shared/ui/toast/toast.service";

@Component({
    selector: 'app-attribute-form-playground-page',
    imports: [JsonPipe, MatButton, MatSlideToggle, DynamicAttributeForm],
    templateUrl: './attribute-form-playground.html',
    styleUrl: './attribute-form-playground.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AttributeFormPlaygroundPage {

    private readonly toast = inject(ToastService);

    protected readonly definitions = LAPTOP_ATTRIBUTE_DEFINITIONS;
    protected readonly values = signal<AttributeValues>(LAPTOP_ATTRIBUTE_VALUES);
    protected readonly disabled = signal(false);
    protected readonly attributesGroup = new FormGroup({});
    protected readonly payload = signal<AttributeValues | null>(null);

    protected toggleDisabled(checked: boolean): void {
        this.disabled.set(checked);
    }

    protected resetToEmpty(): void {
        this.values.set({});
        this.payload.set(null);
    }

    protected submit(): void {
        if (this.attributesGroup.invalid) {
            this.attributesGroup.markAllAsTouched();
            this.toast.warning('Còn thuộc tính chưa hợp lệ');
            return;
        }
        this.payload.set(buildAttributesPayload(this.attributesGroup));
        this.toast.success('Dữ liệu hợp lệ - xem payload bên dưới');
    }
}