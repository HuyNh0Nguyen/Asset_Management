import { ChangeDetectionStrategy, Component, computed, effect, input, untracked } from "@angular/core";
import { FormControl, FormGroup, ReactiveFormsModule } from "@angular/forms";
import { MatError, MatFormField, MatLabel, MatSuffix } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { MatRadioButton, MatRadioGroup } from "@angular/material/radio";
import { MatOption, MatSelect } from "@angular/material/select";
import { AttributeDataType, AttributeDefinitionResponse, CatalogStatus } from "@api/models";
import { AttributeValue, AttributeValues } from "./attribute-values.model";
import { YesNoLabel } from "@core/i18n/labels.vi";
import { createAttributeControl, sortDefinitions } from "./attribute-form.utils";

// Một dòng chỉ đọc cho thuộc tính đã ngừng dùng nhưng tài sản vẫn mang giá trị
interface InactiveAttributeView {
    code: string;
    label: string;
    display: string;
}

@Component({
    selector: 'app-dynamic-attribute-form',
    imports: [ReactiveFormsModule, MatFormField, MatLabel, MatError, MatSuffix, MatInput, MatSelect, MatOption, MatRadioGroup, MatRadioButton],
    templateUrl: './dynamic-attribute-form.html',
    styleUrl: './dynamic-attribute-form.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DynamicAttributeForm {

    // Định nghĩa thuộc tính của loại tài sản
    readonly definitions = input.required<AttributeDefinitionResponse[]>();
    // Giá trị hiện có (attributes_json); tạo mới thì để rỗng
    readonly values = input<AttributeValues>({});
    // FormGroup do màn hình cha tạo; component tự thêm / bỏ control
    readonly group = input.required<FormGroup>();
    readonly disabled = input(false);

    protected readonly DataType = AttributeDataType;
    protected readonly YesNo = YesNoLabel;

    protected readonly activeDefinitions = computed(() =>
        sortDefinitions(this.definitions().filter((definition) => definition.status === CatalogStatus.ACTIVE)),
    );

    protected readonly inactiveAttributes = computed<InactiveAttributeView[]>(() => {
        const values = this.values();
        return sortDefinitions(this.definitions())
            .filter((definition) => definition.status === CatalogStatus.INACTIVE && values[definition.code] !== undefined)
            .map((definition) => ({
                code: definition.code,
                label: definition.label,
                display: this.formatValue(definition, values[definition.code]),
            }));
    });

    constructor() {
        // Dựng lại control mỗi khi định nghĩa / giá trị / trạng thái khóa thay đổi
        effect(() => {
            const group = this.group();
            const definitions = this.activeDefinitions();
            const values = this.values();
            const disabled = this.disabled();
            untracked(() => this.rebuildControls(group, definitions, values, disabled));
        });
    }

    protected controlOf(code: string): FormControl | null {
        return (this.group().get(code) as FormControl | null) ?? null;
    }

    private rebuildControls(
        group: FormGroup,
        definitions: AttributeDefinitionResponse[],
        values: AttributeValues,
        disabled: boolean,
    ): void {
        Object.keys(group.controls).forEach((code) => group.removeControl(code, { emitEvent: false }));
        definitions.forEach((definition) =>
            group.addControl(definition.code, createAttributeControl(definition, values[definition.code], disabled), {
                emitEvent: false,
            }),
        );
        group.updateValueAndValidity();
    }

    // Hiển thị giá trị chỉ đọc: SELECT ra nhãn, BOOLEAN ra Có/Không, NUMBER kèm đơn vị
    private formatValue(definition: AttributeDefinitionResponse, value: AttributeValue): string {
        switch (definition.dataType) {
            case AttributeDataType.SELECT:
                return definition.options.find((option) => option.code === value)?.label ?? String(value);
            case AttributeDataType.BOOLEAN:
                return value ? YesNoLabel.YES : YesNoLabel.NO;
            case AttributeDataType.NUMBER:
                return definition.measureUnit ? `${value} ${definition.measureUnit}` : String(value);
            default:
                return String(value);
        }
    }
}