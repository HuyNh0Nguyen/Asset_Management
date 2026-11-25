import { Routes } from '@angular/router';
import { authGuard, guestGuard, homeGuard, sessionGuard } from '@core/auth/auth.guard';
import { Shell } from '@core/layout/shell/shell';
import { RoutePath } from '@core/routing/route.path';

// Chỉ tệp này được tham chiếu features - luôn qua import() động (lazy-load)
export const routes: Routes = [
    // Ngoài Shell, khai trước route '' để khớp trước
    {
        path: RoutePath.LOGIN,
        canMatch: [guestGuard],
        loadComponent: () => import('@features/auth/pages/login/login-page').then((m) => m.LoginPage),
    },
    {
        path: RoutePath.CHANGE_PASSWORD,
        canActivate: [sessionGuard],
        loadComponent: () =>
            import('@features/auth/pages/change-password/change-password-page').then((m) => m.ChangePasswordPage),
    },
    {
        path: '',
        component: Shell,
        canActivate: [authGuard],
        // Chạy lại authGuard mỗi lần điều hướng: phiên đổi ở tab khác vẫn được kiểm
        runGuardsAndResolvers: 'always',
        children: [
            { path: '', pathMatch: 'full', canActivate: [homeGuard], children: [] },
            { path: RoutePath.ORGANIZATION, loadChildren: () => import('@features/organization/organization.routes') },
            { path: RoutePath.USERS, loadChildren: () => import('@features/users/users.routes') },
            { path: RoutePath.ROLES, loadChildren: () => import('@features/roles/roles.routes') },
            { path: RoutePath.ME, loadChildren: () => import('@features/me/me.routes') },
            { path: RoutePath.DEV_TOOLS, loadChildren: () => import('@features/dev-tools/dev-tools.routes') },
        ],
    },
    { path: '**', redirectTo: '' },
];
