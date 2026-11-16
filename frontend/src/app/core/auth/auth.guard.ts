import { inject } from "@angular/core";
import { CanActivateFn, CanMatchFn, Router } from "@angular/router";
import { SessionStore } from "./session.store";
import { loginUrlTree } from "./return-url";
import { catchError, map, of } from "rxjs";
import { AppUrl } from "@core/routing/route.path";

// Shell: cần token + /api/me; cờ bắt buộc đổi mật khẩu -> trang đổi mật khẩu
export const authGuard: CanActivateFn = (_route, state) => {
    const session = inject(SessionStore);
    const router = inject(Router);
    if (!session.isAuthenticated()) {
        return loginUrlTree(router, state.url);
    }
    return session.ensureMe().pipe(
        map((me) => (me.mustChangePassword ? router.parseUrl(AppUrl.CHANGE_PASSWORD) : true)),
        // 401 đã được error.interceptor xóa phiên + chuyển về đăng nhập
        catchError(() => of(false)),
    );
};

// Trang đổi mật khẩu: chỉ cần phiên, KHÔNG chuyển hướng treo cờ (tránh vòng)
export const sessionGuard: CanActivateFn = (_route, state) => {
    const session = inject(SessionStore);
    const router = inject(Router);
    if (!session.isAuthenticated()) {
        return loginUrlTree(router, state.url);
    }
    return session.ensureMe().pipe(
        map(() => true),
        catchError(() => of(false)),
    );
};

// Trang đăng nhập: đã có phiên thì về trang chủ
export const guestGuard: CanMatchFn = () =>
    inject(SessionStore).isAuthenticated() ? inject(Router).parseUrl(AppUrl.HOME) : true;

// Route '' trong Shell: chạy sau authGuard nên /api/me đã nạp
export const homeGuard: CanActivateFn = () => inject(Router).parseUrl(inject(SessionStore).homeUrl());