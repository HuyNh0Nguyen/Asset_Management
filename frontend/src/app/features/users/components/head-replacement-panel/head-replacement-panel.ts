import { ChangeDetectionStrategy, Component, effect, inject, input, signal, untracked } from "@angular/core";
import { FormControl, ReactiveFormsModule } from "@angular/forms";
import { OrgUnitRefResponse } from "@api/models";
import { HeadReplacementForm } from "../../data/head-replacement-form";
import { UsersStore } from "../../data/users.store";
import { UserMessage } from "../../user-messages";
import { UserPicker } from "@shared/forms/user-picker/user-picker";
import { UserPickerOption, UserPickerValue } from "@shared/forms/user-picker/user-picker.model";
import { finalize } from "rxjs";

// Mỗi đơn vị do một user-picker: ứng viên = người ACTIVE trong cây con, trừ chính người đang sửa / vô hiệu hóa
// Control do bên dùng tạo (syncHeadReplacementForm) - panel chỉ hiển thị và nạp ứng viên
@Component({
    selector: 'app-head-replacement-panel',
    imports: [ReactiveFormsModule, UserPicker],
    templateUrl: './head-replacement-panel.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HeadReplacementPanel {

    private readonly store = inject(UsersStore);

    readonly units = input.required<readonly OrgUnitRefResponse[]>();
    readonly form = input.required<HeadReplacementForm>();
    readonly excludeUserId = input.required<string>();
    readonly serverError = input<string | null>(null);

    protected readonly noHeadOption = UserMessage.NO_HEAD_OPTION;
    protected readonly candidates = signal<Record<string, UserPickerOption[]>>({});
    protected readonly loading = signal<Record<string, boolean>>({});

    constructor() {
        // Đơn vị mới xuất hiện -> nạp ứng viên một lần
        effect(() => {
            const units = this.units();
            const excludeUserId = this.excludeUserId();
            untracked(() => units
                .filter((unit) => !(unit.id in this.candidates()) && !this.loading()[unit.id])
                .forEach((unit) => this.load(unit.id, excludeUserId)));
        });
    }

    // Gọi sau 422 ORG_UNIT_HEAD_*: ứng viên vừa đổi
    reloadCandidates(): void {
        this.candidates.set({});
        this.units().forEach((unit) => this.load(unit.id, this.excludeUserId()));
    }

    protected controlOf(unitId: string): FormControl<UserPickerValue> {
        return this.form().controls[unitId];
    }

    private load(unitId: string, excludeUserId: string): void {
        this.loading.update((state) => ({ ...state, [unitId]: true }));
        this.store.headCandidates(unitId)
            .pipe(finalize(() => this.loading.update((state) => ({ ...state, [unitId]: false }))))
            .subscribe({
                next: (list) => this.candidates.update((state) => ({
                    ...state,
                    [unitId]: list.filter((candidate) => candidate.id !== excludeUserId),
                })),
                error: () => this.candidates.update((state) => ({ ...state, [unitId]: [] })),
            });
    }
}