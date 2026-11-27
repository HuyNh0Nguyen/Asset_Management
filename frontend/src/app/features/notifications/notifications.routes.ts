import { Routes } from "@angular/router";
import { NotificationListStore } from "./data/notification-list.store";

// Mọi người đã đăng nhập - không khai quyền
const routes: Routes = [
    {
        path: '',
        providers: [NotificationListStore],
        loadComponent: () =>
            import('./pages/notification-list/notification-list-page').then((m) => m.NotificationListPage),
    },
];

export default routes;