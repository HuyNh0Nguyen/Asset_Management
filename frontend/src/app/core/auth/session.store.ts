import { computed, DestroyRef, inject, Injectable, signal } from "@angular/core";
import { Router } from "@angular/router";
import { AuthService, MeService } from "@api/services";
import { readStoredToken, removedStoredToken, writeStoredToken } from "./token-storage";
import { DataScope, LoginResponse, MeResponse, Permission } from "@api/models";
import { catchError, finalize, Observable, of, shareReplay, tap } from "rxjs";
import { MenuItem } from "@core/layout/menu-item.model";
import { MENU_ITEMS } from "@core/layout/menu-items";
import { AppUrl } from "@core/routing/route.path";
import { StorageKey } from "./auth-header";
import { skipErrorToast } from "@core/http/http-context";

// Lỗi HTTP đã được error.interceptor xử lý - ở đây chỉ nuốt
const ignoreError = (): void => undefined;

@Injectable({ providedIn: 'root' })
export class SessionStore {

    private readonly authApi = inject(AuthService);
    private readonly meApi = inject(MeService);
    private readonly router = inject(Router);

    private readonly tokenState = signal<string | null>(readStoredToken());
    // Không lưu me xuống storage - quyền luôn lấy mới từ /api/me
    private readonly meState = signal<MeResponse | null>(null);
    private meRequest: Observable<MeResponse> | null = null;

    readonly token = this.tokenState.asReadonly();
    readonly me = this.meState.asReadonly();
    readonly isAuthenticated = computed(() => this.tokenState() !== null);
    readonly mustChangePassword = computed(() => this.meState()?.mustChangePassword ?? false);

    // Quyền hiệu lực: mã quyền -> phạm vi
    readonly grants = computed(
        () => new Map<Permission, DataScope>((this.meState()?.permissions ?? []).map((grant) => [grant.code, grant.scope])),
    );

    // Menu dựng theo mã quyền, không theo tên vai trò
    readonly visibleMenuItems = computed<MenuItem[]>(() => MENU_ITEMS.filter((item) => this.hasPermission(item.permission)));

    // TODO: Trang chủ tạm = mục menu đầu tiên người dùng thấy; phase 9 thay bằng Dashboard
    readonly homeUrl = computed<string>(() => this.visibleMenuItems()[0]?.url ?? AppUrl.PROBES);

    constructor() {
        // Tab khác đăng nhập / đăng xuất thì tab này đồng bộ theo
        const onStorage = (event: StorageEvent): void => this.syncFromOtherTab(event);
        window.addEventListener('storage', onStorage);
        inject(DestroyRef).onDestroy(() => window.removeEventListener('storage', onStorage));
    }

    // Không khai quyền = ai đăng nhập cũng thấy
    hasPermission(permission?: Permission): boolean {
        return !permission || this.grants().has(permission);
    }

    // Không toast mặc định: trang đăng nhập hiện lỗi dưới form
    login(username: string, password: string): Observable<LoginResponse> {
        return this.authApi.login({ body: { username, password } }, skipErrorToast()).pipe(
            tap((response) => {
                this.meState.set(null);
                this.meRequest = null;
                this.tokenState.set(response.accessToken);
                writeStoredToken(response.accessToken);
            }),
        );
    }

    // Nạp /api/me một lần; nhiều guard gọi cùng lúc chỉ tạo một request
    ensureMe(): Observable<MeResponse> {
        const current = this.meState();
        if (current) {
            return of(current);
        }
        if (!this.meRequest) {
            this.meRequest = this.meApi.getMe().pipe(
                tap((me) => this.applyMe(me)),
                finalize(() => (this.meRequest = null)),
                shareReplay({ bufferSize: 1, refCount: false }),
            );
        }
        return this.meRequest;
    }

    // Gọi khi nhận 403: quyền có thể sửa đổi
    reloadMe(): void {
        if (!this.tokenState()) {
            return;
        }
        this.meApi.getMe().subscribe({ next: (me) => this.applyMe(me), error: ignoreError });
    }

    // Gọi API, bỏ qua lỗi (mạng, token đã bị thu hồi), luôn xóa phiên
    logout(): Observable<void> {
        if (!this.tokenState()) {
            this.clear();
            return of(undefined);
        }
        return this.authApi.logout(undefined, skipErrorToast()).pipe(
            catchError(() => of(undefined)),
            finalize(() => this.clear()),
        );
    }

    clear(): void {
        removedStoredToken();
        this.tokenState.set(null);
        this.meState.set(null);
        this.meRequest = null;
    }

    // Phiên đã bị xóa giữa chừng thì bỏ kết quả /api/me về muộn
    private applyMe(me: MeResponse): void {
        if (this.tokenState()) {
            this.meState.set(me);
        }
    }

    private syncFromOtherTab(event: StorageEvent): void {
        // key null = tab khác xóa toàn bộ storage
        if (event.key !== null && event.key !== StorageKey.ACCESS_TOKEN) {
            return;
        }
        const next = readStoredToken();
        if (next === this.tokenState()) {
            return;
        }
        this.tokenState.set(next);
        this.meState.set(null);
        this.meRequest = null;
        void this.router.navigateByUrl(next ? AppUrl.HOME : AppUrl.LOGIN);
    }
}