import { Router, UrlTree } from "@angular/router";
import { AppUrl, QueryParamKey } from "@core/routing/route.path";

// Không quay về chính trang xác thực sau khi đăng nhập
const AUTH_URLS: readonly string[] = [AppUrl.LOGIN, AppUrl.CHANGE_PASSWORD];

export function loginUrlTree(router: Router, returlUrl: string): UrlTree {
    const target = safeReturnUtl(returlUrl);
    const queryParams = target === AppUrl.HOME ? {} : { [QueryParamKey.RETURN_URL]: target };
    return router.createUrlTree([AppUrl.LOGIN], { queryParams });
}

// Chỉ nhận đường dẫn nội bộ - chặn chuyển hướng ra domain khác
export function safeReturnUtl(value: string | null | undefined): string {
    if (!value || !value.startsWith(AppUrl.HOME) || value.startsWith('//')) {
        return AppUrl.HOME;
    }
    return AUTH_URLS.some((url) => value.startsWith(url)) ? AppUrl.HOME : value;
}