import { Routes } from "@angular/router";
import { Permission } from "@api/models";
import { permissionGuard } from "@core/auth/permission.guard";
import { RouteDataKey } from "@core/routing/route.path";
import { UsersStore } from "./data/users.store";

const routes: Routes = [
    {
        path: '',
        canActivate: [permissionGuard],
        data: { [RouteDataKey.PERMISSION]: Permission.USER_MANAGE },
        // Store theo vòng đời route; hộp thoại dùng được qua viewContainerRef của trang
        providers: [UsersStore],
        loadComponent: () => import('./pages/user-list/user-list-page').then((m) => m.UserListPage),
    },
];

export default routes;