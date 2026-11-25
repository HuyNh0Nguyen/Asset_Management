import { computed, inject, Injectable, signal } from "@angular/core";
import { AvatarResponse, CreateUserRequest, DeactivateUserRequest, HeadReplacementCheckResponse, OrgUnitHeadResponse, OrgUnitNodeResponse, RankResponse, ReactivateUserRequest, RoleResponse, UpdateUserRequest, UserDeactivationCheckResponse, UserResponse } from "@api/models";
import { PageResponseUserResponse } from "@api/models/page-response-user-response";
import { OrganizationService, RolesService, UsersService } from "@api/services";
import { toListUsersParams, UserListQuery } from "./user-list-query";
import { finalize, Observable, Subscription } from "rxjs";
import { skipErrorToast } from "@core/http/http-context";

// Lỗi HTTP đã được error.interceptor hiện toast - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

// Trang hiện tại + dữ liệu tham chiếu. features/users không import features/organization
@Injectable()
export class UsersStore {

    private readonly usersApi = inject(UsersService);
    private readonly rolesApi = inject(RolesService);
    private readonly organizationApi = inject(OrganizationService);

    private readonly pageState = signal<PageResponseUserResponse | null>(null);
    private readonly loadingState = signal(false);
    private readonly orgUnitsState = signal<OrgUnitNodeResponse[]>([]);
    private readonly ranksState = signal<RankResponse[]>([]);
    private readonly rolesState = signal<RoleResponse[]>([]);
    private lastQuery: UserListQuery | null = null;
    private listRequest: Subscription | null = null;

    readonly users = computed<UserResponse[]>(() => this.pageState()?.items ?? []);
    readonly total = computed(() => this.pageState()?.totalElements ?? 0);
    readonly loading = this.loadingState.asReadonly();
    readonly orgUnits = this.orgUnitsState.asReadonly();
    readonly ranks = this.ranksState.asReadonly();
    readonly roles = this.rolesState.asReadonly();

    // Cây đơn vị, cấp bậc, vai trò cho bộ lọc và form
    loadReferences(): void {
        this.reloadOrgUnits();
        this.organizationApi.listRanks().subscribe({ next: (ranks) => this.ranksState.set(ranks), error: ignoreError });
        this.rolesApi.listRoles().subscribe({ next: (roles) => this.rolesState.set(roles), error: ignoreError });
    }

    // Gọi sau 422 ORG_UNIT_INACTIVE: đơn vị vừa ngừng hoạt động
    reloadOrgUnits(): void {
        this.organizationApi.getOrgUnitTree()
            .subscribe({ next: (nodes) => this.orgUnitsState.set(nodes), error: ignoreError });
    }

    // Lọc mới thì bỏ kết quả của lần lọc trước
    load(query: UserListQuery): void {
        this.lastQuery = query;
        this.listRequest?.unsubscribe();
        this.loadingState.set(true);
        this.listRequest = this.usersApi.listUsers(toListUsersParams(query))
            .pipe(finalize(() => this.loadingState.set(false)))
            .subscribe({ next: (page) => this.pageState.set(page), error: ignoreError });
    }

    reload(): void {
        if (this.lastQuery) {
            this.load(this.lastQuery);
        }
    }

    // Lệnh ghi trả Observable - hộp thoại tự xử lý lỗi có nghĩa riêng
    create(body: CreateUserRequest): Observable<UserResponse> {
        return this.usersApi.createUser({ body }, skipErrorToast());
    }

    update(id: string, body: UpdateUserRequest): Observable<UserResponse> {
        return this.usersApi.updateUser({ id, body }, skipErrorToast());
    }

    checkUnitChange(id: string, orgUnitId: string): Observable<HeadReplacementCheckResponse> {
        return this.usersApi.checkUserUnitChange({ id, orgUnitId }, skipErrorToast());
    }

    // Dùng lại endpoint của đơn vị (ORG_MANAGE hoặc USER_MANAGE_
    headCandidates(unitId: string): Observable<OrgUnitHeadResponse[]> {
        return this.organizationApi.listOrgUnitHeadCandidates({ id: unitId });
    }

    // 409 (đã vô hiệu hóa) để interceptor toast
    checkDeactivation(id: string): Observable<UserDeactivationCheckResponse> {
        return this.usersApi.checkUserDeactivation({ id });
    }

    deactivate(id: string, body: DeactivateUserRequest): Observable<void> {
        return this.usersApi.deactivateUser({ id, body }, skipErrorToast());
    }

    reactivate(id: string, body: ReactivateUserRequest): Observable<void> {
        return this.usersApi.reactivateUser({ id, body }, skipErrorToast());
    }

    resetPassword(id: string): Observable<void> {
        return this.usersApi.resetUserPassword({ id });
    }

    // Hộp thoại tải ảnh hiện lỗi tệp tại chỗ
    changeAvatar(id: string, file: File): Observable<AvatarResponse> {
        return this.usersApi.changeUserAvatar({ id, body: { file } }, skipErrorToast());
    }

    removeAvatar(id: string): Observable<void> {
        return this.usersApi.removeUserAvatar({ id });
    }
}