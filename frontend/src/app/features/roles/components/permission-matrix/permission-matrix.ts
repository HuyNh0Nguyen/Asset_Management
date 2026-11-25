import { ChangeDetectionStrategy, Component, computed, forwardRef, input, signal } from "@angular/core";
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from "@angular/forms";
import { MatRadioButton } from "@angular/material/radio";
import { DataScope, Permission, PermissionDefinitionResponse, RoleGrantRequest } from "@api/models";
import { DATA_SCOPE_LABELS, PERMISSION_DESCRIPTIONS, PERMISSION_LABELS } from "@core/i18n/labels.vi";
import { groupDefinitions, SELF_COVERS_NOTHING } from "../../data/permission-groups";
import { RoleMessage } from "../../roles-messages";

// Ma trận quyền của một vai trò. Giá trị = RoleGrantRequest[]; "Không cấp" = không có ô.
// Mọi giới hạn (phạm vi hợp lệ, adminOnly) đọc từ GET /api/permissions - FE không giữ danh sách nào
@Component({
    selector: 'app-permission-matrix',
    imports: [MatRadioButton],
    templateUrl: './permission-matrix.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
    providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => PermissionMatrix), multi: true }],
})
export class PermissionMatrix implements ControlValueAccessor {

    readonly definitions = input.required<readonly PermissionDefinitionResponse[]>();
    readonly readonly = input(false);

    protected readonly groups = computed(() => groupDefinitions(this.definitions()));
    protected readonly scopes = Object.values(DataScope);
    protected readonly scopeLabels = DATA_SCOPE_LABELS;
    protected readonly permissionLabels = PERMISSION_LABELS;
    protected readonly descriptions = PERMISSION_DESCRIPTIONS;
    protected readonly selfNote = RoleMessage.SELF_COVERS_NOTHING;
    protected readonly adminOnlyNote = RoleMessage.ADMIN_ONLY;

    private readonly selection = signal<ReadonlyMap<Permission, DataScope>>(new Map());
    private readonly disabledState = signal(false);
    protected readonly locked = computed(() => this.readonly() || this.disabledState());

    private onChange: (value: RoleGrantRequest[]) => void = () => undefined;
    private onTouched: () => void = () => undefined;

    protected scopeOf(permission: Permission): DataScope | undefined {
        return this.selection().get(permission);
    }

    // Hàng adminOnly khóa ở vai trò thường
    protected isRowDisabled(definition: PermissionDefinitionResponse): boolean {
        return this.locked() || definition.adminOnly;
    }

    protected isScopeDisabled(definition: PermissionDefinitionResponse, scope: DataScope): boolean {
        return this.isRowDisabled(definition) || !definition.allowedScopes.includes(scope);
    }

    protected showSelfNote(definition: PermissionDefinitionResponse, scope: DataScope): boolean {
        return scope === DataScope.SELF && SELF_COVERS_NOTHING.has(definition.code)
            && definition.allowedScopes.includes(scope);
    }

    protected select(permission: Permission, scope: DataScope | null): void {
        const next = new Map(this.selection());
        if (scope) {
            next.set(permission, scope);
        } else {
            next.delete(permission);
        }
        this.selection.set(next);
        this.onChange(this.toValue(next));
        this.onTouched();
    }

    writeValue(value: RoleGrantRequest[] | null): void {
        this.selection.set(new Map((value ?? []).map((grant) => [grant.permission, grant.scope])));
    }

    registerOnChange(fn: (value: RoleGrantRequest[]) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    setDisabledState(disabled: boolean): void {
        this.disabledState.set(disabled);
    }

    // Theo thứ tự quyền của API
    private toValue(selection: ReadonlyMap<Permission, DataScope>): RoleGrantRequest[] {
        return this.definitions().flatMap((definition) => {
            const scope = selection.get(definition.code);
            return scope ? [{ permission: definition.code, scope }] : [];
        });
    }
}