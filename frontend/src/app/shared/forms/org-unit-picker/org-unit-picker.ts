import { ChangeDetectionStrategy, Component, computed, effect, forwardRef, input, signal, untracked } from "@angular/core";
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR, ReactiveFormsModule } from "@angular/forms";
import { MatAutocomplete, MatAutocompleteSelectedEvent, MatAutocompleteTrigger } from "@angular/material/autocomplete";
import { MatOption } from "@angular/material/core";
import { MatFormField, MatHint, MatLabel } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { OrgUnitPickerMode, OrgUnitPickerOption, OrgUnitSelectable } from "./org-unit-picker.model";
import { OrgUnitNodeResponse, OrgUnitStatus } from "@api/models";
import { takeUntilDestroyed, toSignal } from "@angular/core/rxjs-interop";
import { matchesOrgUnit } from "./org-tree.utils";
import { ORG_UNIT_STATUS_LABELS } from "@core/i18n/labels.vi";
import { normalizeSearchText } from "@shared/text/normalize-search-text";

// Thụt lề mỗi cấp trong danh sách gợi ý (px)
const DEPTH_INDENT_PX = 16;
const BASE_PADDING_PX = 16;

// Bộ chọn đơn vị dùng chung (người dùng, nhập tài sản, điều chuyển). Giá trị = id đơn vị.
// Không gọi API: bên dùng tự nạp và truyền nodes
@Component({
    selector: 'app-org-unit-picker',
    imports: [ReactiveFormsModule, MatFormField, MatLabel, MatHint, MatInput, MatAutocomplete, MatAutocompleteTrigger, MatOption],
    templateUrl: './org-unit-picker.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => OrgUnitPicker), multi: true }],
})
export class OrgUnitPicker implements ControlValueAccessor {

    readonly nodes = input.required<readonly OrgUnitNodeResponse[]>();
    readonly mode = input<OrgUnitPickerMode>(OrgUnitPickerMode.WRITE);
    readonly selectable = input<OrgUnitSelectable>(() => true);
    readonly label = input('Đơn vị');
    readonly hint = input('');

    // Chỉ hiển thị danh sách cây khi người dùng tương tác vào ô tìm kiếm
    protected readonly initialized = signal(false);

    // Ô nhập: chuỗi khi đang gõ, nút khi đã chọn
    protected readonly textControl = new FormControl<string | OrgUnitNodeResponse | null>('');
    private readonly selectedId = signal<string | null>(null);
    private readonly textValue = toSignal(this.textControl.valueChanges, { initialValue: this.textControl.value });

    private readonly query = computed(() => {
        const value = this.textValue();
        return typeof value === 'string' ? normalizeSearchText(value) : '';
    });

    protected readonly options = computed<OrgUnitPickerOption[]>(() => {
        if (!this.initialized()) {
            return [];
        }
        const query = this.query();
        const canSelect = this.selectable();
        const writeMode = this.mode() === OrgUnitPickerMode.WRITE;
        return this.nodes()
            // Chế độ ghi: đơn vị ngừng hoạt động vắng mặt - yêu cầu này chỉ nằm ở đây
            .filter((node) => !writeMode || node.status === OrgUnitStatus.ACTIVE)
            .filter((node) => !query || matchesOrgUnit(node, query))
            .map((node) => ({ node, selectable: canSelect(node) }));         
    });

    protected readonly inactiveSuffix = ` · ${ORG_UNIT_STATUS_LABELS[OrgUnitStatus.INACTIVE]}`;
    protected readonly inactiveStatus = OrgUnitStatus.INACTIVE;

    private onChange: (value: string | null) => void = () => undefined;
    private onTouched: () => void = () => undefined;

    constructor() {
        // Gõ lại sau khi đã chọn -> bỏ lựa chọn cũ
        this.textControl.valueChanges.pipe(takeUntilDestroyed()).subscribe((value) => {
            if (typeof value === 'string') {
                this.initPicker();
                if (this.selectedId() !== null) {
                    this.selectedId.set(null);
                    this.onChange(null);
                }
            }
        });
        // writeValue có thể tới trước nodes - hiển thị tên khi đủ cả hai
        effect(() => {
            const id = this.selectedId();
            const node = id ? this.nodes().find((item) => item.id === id) : undefined;
            untracked(() => {
                const current = this.textControl.value;
                if (node && (typeof current !== 'object' || current?.id !== node.id)) {
                    this.textControl.setValue(node, { emitEvent: false });
                }
            });
        });
    }

    protected readonly displayWith = (value: string | OrgUnitNodeResponse | null): string =>
        typeof value === 'string' ? value : value ? `${value.name} (${value.code})` : '';

    protected indentOf(depth: number): number {
        return BASE_PADDING_PX + depth * DEPTH_INDENT_PX;
    }

    protected initPicker(): void {
        if (!this.initialized()) {
            this.initialized.set(true);
        }
    }

    protected onSelected(event: MatAutocompleteSelectedEvent): void {
        const node = event.option.value as OrgUnitNodeResponse;
        this.selectedId.set(node.id);
        this.onChange(node.id);
        this.onTouched();
    }

    protected onBlur(): void {
        this.onTouched();
    }

    writeValue(value: string | null): void {
        this.selectedId.set(value);
        if (!value) {
            this.textControl.setValue('', { emitEvent: false });
        }
    }

    registerOnChange(fn: (value: string | null) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(disabled: boolean): void {
        if (disabled) {
            this.textControl.disable({ emitEvent: false });
        } else {
            this.textControl.enable({ emitEvent: false });
        }
    }
}