import { HttpErrorResponse, HttpInterceptorFn, HttpRequest, HttpStatusCode } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";
import { parseErrorBody } from "./error-body";
import { SKIP_ERROR_TOAST } from "./http-context";
import { AppUrl } from "@core/routing/route.path";
import { ToastService } from "@shared/ui/toast/toast.service";
import { resolveErrorMessage } from "@core/i18n/error-messages.vi";
import { AuthHeader } from "@core/auth/auth-header";
import { SessionStore } from "@core/auth/session.store";
import { loginUrlTree } from "@core/auth/return-url";
import { ErrorCode } from "@api/models";
import { MeService } from "@api/services";

// Rẽ nhánh theo mã HTTP, hiển thị theo "code" - không bao giờ hiển thị "message"
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const toast = inject(ToastService);
    const router = inject(Router);
    // inject trong thân hàm: tránh vòng DI HttpClient <-> SessionStore
    const session = inject(SessionStore);

    return next(req).pipe(
        catchError((error: unknown) => {
            if (error instanceof HttpErrorResponse) {
                const code = parseErrorBody(error.error)?.code;
                const carriedToken = req.headers.has(AuthHeader.NAME);
                const isUnauthorized = error.status === HttpStatusCode.Unauthorized;
                // Nhiều request cùng 401: chỉ request đầu tiên báo và chuyển trang
                const sessionAlreadyEnded = isUnauthorized && carriedToken && !session.isAuthenticated();
                
                if (!req.context.get(SKIP_ERROR_TOAST) && !sessionAlreadyEnded) {
                    toast.error(resolveErrorMessage(error.status, code));
                }
                // Request đăng nhập không mang token -> lỗi ở lại form
                if (isUnauthorized && carriedToken && !sessionAlreadyEnded) {
                    endSession(session, router);
                }
                if (error.status === HttpStatusCode.Forbidden) {
                    handleForbidden(req, code, session, router);
                }
            }
            return throwError(() => error);
        }),
    );

    // Token hết hạn / bị thu hồi -> xóa phiên, về đăng nhập kèm returnUrl
    function endSession(session: SessionStore, router: Router): void {
        const returnUrl = router.url;
        session.clear();
        if (!returnUrl.startsWith(AppUrl.LOGIN)) {
            void router.navigateByUrl(loginUrlTree(router, returnUrl));
        }
    }

    // Cần đổi mật khẩu -> trang đổi mật khẩu; 403 khác -> quyền có thể vừa đổi, nạp lại /api/me
    function handleForbidden(req: HttpRequest<unknown>, code: ErrorCode | undefined,
                            session: SessionStore, router: Router): void {
        if (code === ErrorCode.PASSWORD_CHANGE_REQUIRED) {
            void router.navigateByUrl(AppUrl.CHANGE_PASSWORD);
            return;
        }
        if (!req.url.endsWith(MeService.GetMePath)) {
            session.reloadMe();
        }
    }
};