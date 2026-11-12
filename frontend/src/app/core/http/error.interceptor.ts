import { HttpErrorResponse, HttpInterceptorFn, HttpStatusCode } from "@angular/common/http";
import { inject } from "@angular/core";
import { Router } from "@angular/router";
import { catchError, throwError } from "rxjs";
import { parseErrorBody } from "./error-body";
import { SKIP_ERROR_TOAST } from "./http-context";
import { AppUrl } from "@core/routing/route.path";
import { ToastService } from "@shared/ui/toast/toast.service";
import { resolveErrorMessage } from "@core/i18n/error-messages.vi";

// Rẽ nhánh theo mã HTTP, hiển thị theo "code" - không bao giờ hiển thị "message"
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
    const toast = inject(ToastService);
    const router = inject(Router);

    return next(req).pipe(
        catchError((error: unknown) => {
            if (error instanceof HttpErrorResponse) {
                const body = parseErrorBody(error.error);
                if (!req.context.get(SKIP_ERROR_TOAST)) {
                    toast.error(resolveErrorMessage(error.status, body?.code));
                }
                if (error.status === HttpStatusCode.Unauthorized) {
                    // TODO: Màn hình đăng nhập làm ở phase 1
                    void router.navigateByUrl(AppUrl.LOGIN);
                }
                // TODO: 403: Phase 2 nạp lại /api/me vì quyền có thể vừa đổi
            }
            return throwError(() => error);
        }),
    );
};