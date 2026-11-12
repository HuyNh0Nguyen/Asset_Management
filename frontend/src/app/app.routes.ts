import { Routes } from '@angular/router';
import { Shell } from '@core/layout/shell/shell';
import { RoutePath } from '@core/routing/route.path';

// Chỉ tệp này được tham chiếu features - luôn qua import() động (lazy-load)
export const routes: Routes = [
    {
        path: '',
        component: Shell,
        children: [
            { path: '', pathMatch: 'full', redirectTo: RoutePath.DEV_TOOLS },
            { path: RoutePath.DEV_TOOLS, loadChildren: () => import('@features/dev-tools/dev-tools.routes') },
        ],
    },
    // TODO (Phase 1): thêm route đăng nhập ngoài Shell
    { path: '**', redirectTo: '' },
];
