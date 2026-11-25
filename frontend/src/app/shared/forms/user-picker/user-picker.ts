
// Chọn một người từ danh sách truyền vào + lựa chọn "Để trống". Không gọi API.

import { ChangeDetectionStrategy, ChangeDetectorRef, Component, computed, DestroyRef, inject, input, OnInit, signal } from "@angular/core";
import { ControlValueAccessor, FormControl, NgControl, ReactiveFormsModule } from "@angular/forms";
import { MatAutocomplete, MatAutocompleteSelectedEvent, MatAutocompleteTrigger, MatOption } from "@angular/material/autocomplete";
import { MatError, MatFormField, MatHint, MatLabel } from "@angular/material/form-field";
import { MatInput } from "@angular/material/input";
import { Avatar } from "@shared/media/avatar/avatar";
import { UserPickerErrorKey, UserPickerOption, UserPickerText, UserPickerValue } from "./user-picker.model";
import { takeUntilDestroyed, toSignal } from "@angular/core/rxjs-interop";
import { AvatarSize } from "@shared/media/avatar-size";
import { normalizeSearchText } from "@shared/text/normalize-search-text";
import { ErrorStateMatcher } from "@angular/material/core";

// Dùng ở: hộp thoại đơn vị (trưởng), form người dùng và hộp thoại vô hiệu hóa (trưởng thay thế)
@Component({
    selector: 'app-user-picker',
    imports: [
        ReactiveFormsModule,
        MatFormField,
        MatLabel,
        MatHint,
        MatError,
        MatInput,
        MatAutocomplete,
        MatAutocompleteTrigger,
        MatOption,
        Avatar,
    ],
    templateUrl: './user-picker.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UserPicker implements ControlValueAccessor, OnInit {

    readonly candidates = input.required<readonly UserPickerOption[]>();
    readonly label = input('Người dùng');
    readonly hint = input('');
    readonly loading = input(false);
    readonly allowEmpty = input(true);
    readonly emptyLabel = input<string>(UserPickerText.EMPTY_OPTION);
    // true: xóa trắng ô vẫn là "chưa chọn" - bắt chọn "Để trống" tường minh
    readonly explicitChoice = input(false);
    readonly serverError = input<string | null>(null);

    // Tự gắn làm value accessor để đọc lỗi của control cha
    private readonly ngControl = inject(NgControl, { self: true, optional: true });
    private readonly changeDetector = inject(ChangeDetectorRef);
    private readonly destroyRef = inject(DestroyRef);

    // Chỉ hiển thị danh sách khi người dùng tương tác vào ô tìm kiếm
    protected readonly initialized = signal(false);

    // Ô nhập: chuỗi khi đang gõ, người khi đã chọn, null khi "Để trống"
    protected readonly textControl = new FormControl<string | UserPickerOption | null>('');
    private readonly textValue = toSignal(this.textControl.valueChanges, { initialValue: this.textControl.value });
    protected readonly errorKey = UserPickerErrorKey;
    protected readonly avatarSize = AvatarSize.SMALL;
    protected readonly loadingText = UserPickerText.LOADING;

    // Lọc theo chữ đang gõ: họ tên / username / đơn vị
    protected readonly filtered = computed(() => {
        const value = this.textValue();
        const query = typeof value === 'string' ? normalizeSearchText(value) : '';
        return this.candidates().filter((option) => !query
            || normalizeSearchText(`${option.fullName} ${option.username} ${option.orgUnitName ?? ''}`).includes(query));
    });

    // Viền đỏ theo trạng thái của control cha
    protected readonly errorMatcher: ErrorStateMatcher = {
        isErrorState: () => !!this.ngControl?.invalid && !!(this.ngControl.touched || this.ngControl.dirty),
    };

    private onChange: (value: UserPickerValue) => void = () => undefined;
    private onTouched: () => void = () => undefined;

    constructor() {
        if (this.ngControl) {
            this.ngControl.valueAccessor = this;
        }
        // Gõ chữ -> bỏ lựa chọn cũ
        this.textControl.valueChanges.pipe(takeUntilDestroyed()).subscribe((value) => {
            if (typeof value !== 'string') {
                return;
            }
            this.initPicker();
            const cleared = value.trim() === '';
            this.onChange(cleared && this.allowEmpty() && !this.explicitChoice() ? null : undefined);
        });
    }

    // Cha markAllAsTouched / setErrors -> vẽ lại để hiện lỗi (OnPush)
    ngOnInit(): void {
        this.ngControl?.control?.events
            .pipe(takeUntilDestroyed(this.destroyRef))
            .subscribe(() => this.changeDetector.markForCheck());
    }

    protected readonly displayWith = (value: string | UserPickerOption | null): string => {
        if (typeof value === 'string') {
            return value;
        }
        return value ? `${value.fullName} (${value.username})` : this.emptyLabel();
    };

    protected hasError(key: UserPickerErrorKey): boolean {
        return !!this.ngControl?.hasError(key);
    }

    protected initPicker(): void {
        if (!this.initialized()) {
            this.initialized.set(true);
        }
    }

    protected onSelected(event: MatAutocompleteSelectedEvent): void {
        this.onChange(event.option.value as UserPickerOption | null);
        this.onTouched();
    }

    protected onBlur(): void {
        this.onTouched();
    }

    writeValue(value: UserPickerValue): void {
        this.textControl.setValue(value === undefined ? '' : value, { emitEvent: false });
    }

    registerOnChange(fn: (value: UserPickerValue) => void): void {
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