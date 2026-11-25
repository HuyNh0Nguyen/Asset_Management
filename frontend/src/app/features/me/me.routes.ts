import { Routes } from "@angular/router";

// Không cần quyền: ai đăng nhập cũng xem được tài khoản của mình
const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./pages/my-account/my-account-page').then((m) => m.MyAccountPage),
    },
];

export default routes;