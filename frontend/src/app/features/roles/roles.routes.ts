import { Routes } from "@angular/router";
import { Permission } from "@api/models";
import { permissionGuard } from "@core/auth/permission.guard";
import { RouteDataKey, RoutePath } from "@core/routing/route.path";
import { RolesStore } from "./data/roles.store";

const routes: Routes = [
    { 
        path: '',
        canActivate: [permissionGuard],
        data: { [RouteDataKey.PERMISSION]: Permission.ROLE_MANAGE },
        // Danh sách và trình sửa dùng chung một store (danh mục quyền nạp một lần)
        providers: [RolesStore],
        children: [
            { path: '', loadComponent: () => import('./pages/role-list/role-list-page').then((m) => m.RoleListPage) },
            { path: RoutePath.ROLE_NEW, loadComponent: () => import('./pages/role-editor/role-editor-page').then((m) => m.RoleEditorPage) },
            { path: RoutePath.ROLE_DETAIL, loadComponent: () => import('./pages/role-editor/role-editor-page').then((m) => m.RoleEditorPage) },
        ],
    },
];

export default routes;