import { inject, Injectable, signal } from "@angular/core";
import { CreateRoleRequest, PermissionDefinitionResponse, RoleResponse, UpdateRoleRequest } from "@api/models";
import { RolesService } from "@api/services";
import { skipErrorToast } from "@core/http/http-context";
import { finalize, Observable } from "rxjs";

// Lỗi HTTP đã được error.interceptor hiện toast - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

// Vai trò trả trọn (vài dòng); danh mục quyền cố định - nạp một lần
@Injectable()
export class RolesStore {

    private readonly api = inject(RolesService);

    private readonly rolesState = signal<RoleResponse[]>([]);
    private readonly permissionsState = signal<PermissionDefinitionResponse[]>([]);
    private readonly loadingState = signal(false);
    private permissionRequested = false;

    readonly roles = this.rolesState.asReadonly();
    readonly permissions = this.permissionsState.asReadonly();
    readonly loading = this.loadingState.asReadonly();

    load(): void {
        this.loadingState.set(true);
        this.api.listRoles()
            .pipe(finalize(() => this.loadingState.set(false)))
            .subscribe({ next: (roles) => this.rolesState.set(roles), error: ignoreError });
        this.ensurePermissions();
    }

    ensurePermissions(): void {
        if (this.permissionRequested) {
            return;
        }
        this.permissionRequested = true;
        this.api.listPermissions().subscribe({
            next: (permissions) => this.permissionsState.set(permissions),
            error: () => (this.permissionRequested = false),
        });
    }

    get(id: string): Observable<RoleResponse> {
        return this.api.getRole({ id });
    }

    // Trình sửa tự xử lý lỗi có nghĩa riêng
    create(body: CreateRoleRequest): Observable<RoleResponse> {
        return this.api.createRole({ body }, skipErrorToast());
    }

    update(id: string, body: UpdateRoleRequest): Observable<RoleResponse> {
        return this.api.updateRole({ id, body }, skipErrorToast());
    }

    // 422 ROLE_IN_USE / ROLE_SYSTEM_LOCKED để interceptor toast
    delete(id: string): Observable<void> {
        return this.api.deleteRole({ id });
    }
}