import { Routes } from "@angular/router";
import { Permission } from "@api/models";
import { permissionGuard } from "@core/auth/permission.guard";
import { RouteDataKey, RoutePath } from "@core/routing/route.path";
import { OrgUnitsStore } from "./data/org-units.store";
import { RanksStore } from "./data/ranks.store";

// Cả hai màn hình cần ORG_MANAGE; store cấp route để hộp thoại dùng chung qua viewContainerRef
const organizationRoutes: Routes = [
    {
        path: '',
        canActivate: [permissionGuard],
        data: { [RouteDataKey.PERMISSION]: Permission.ORG_MANAGE },
        providers: [OrgUnitsStore, RanksStore],
        children: [
            { path: '', pathMatch: 'full', redirectTo: RoutePath.ORG_UNITS },
            {
                path: RoutePath.ORG_UNITS,
                loadComponent: () => import('./pages/org-unit-tree/org-unit-tree-page').then((m) => m.OrgUnitTreePage),
            },
            {
                path: RoutePath.RANKS,
                loadComponent: () => import('./pages/rank-list/rank-list-page').then((m) => m.RankListPage),
            },
        ],
    },
];

export default organizationRoutes;