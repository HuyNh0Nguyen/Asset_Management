import { HttpInterceptorFn } from "@angular/common/http";
import { inject } from "@angular/core";
import { SessionStore } from "./session.store";
import { ApiPrefix, AuthHeader } from "./auth-header";
import { AuthService } from "@api/services";

// Gắn Bearer cho request tới backend, trừ đăng nhập
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const token = inject(SessionStore).token();
    const isApiRequest = req.url.startsWith(ApiPrefix.ROOT);
    if (!token || !isApiRequest || req.url.endsWith(AuthService.LoginPath)) {
        return next(req);
    }
    return next(req.clone({ setHeaders: { [AuthHeader.NAME]: `${AuthHeader.BEARER_PREFIX}${token}` } }));
}