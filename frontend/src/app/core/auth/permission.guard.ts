import { inject } from "@angular/core";
import { CanActivateFn, Router } from "@angular/router";
import { SessionStore } from "./session.store";
import { RouteDataKey } from "@core/routing/route.path";
import { ErrorCode, Permission } from "@api/models";
import { ToastService } from "@shared/ui/toast/toast.service";
import { ERROR_MESSAGES_VI } from "@core/i18n/error-messages.vi";

// Đọc route.data.permission; chỉ là trải nghiệm - backend vẫn chặn
export const permissionGuard: CanActivateFn = (route) => {
    const session = inject(SessionStore);
    const permission = route.data[RouteDataKey.PERMISSION] as Permission | undefined;
    if (session.hasPermission(permission)) {
        return true;
    }
    inject(ToastService).error(ERROR_MESSAGES_VI[ErrorCode.FORBIDDEN]);
    return inject(Router).parseUrl(session.homeUrl());
}