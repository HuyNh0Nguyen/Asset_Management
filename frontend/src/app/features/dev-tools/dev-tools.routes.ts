import { Routes } from "@angular/router";
import { RoutePath } from "@core/routing/route.path";

// Trang công cụ của phase 0 - chỉ để kiểm hạ tầng, không phải màn hình nghiệp vụ
export default [
    { path: '', pathMatch: 'full', redirectTo: RoutePath.PROBES },
    {
        path: RoutePath.PROBES,
        loadComponent: () => import('./pages/probes/probes').then((m) => m.ProbesPage),
    },
    {
        path: RoutePath.ATTRIBUTE_FORM,
        loadComponent: () =>
            import('./pages/attribute-form-playground/attribute-form-playground').then((m) => m.AttributeFormPlaygroundPage),
    },
] satisfies Routes;