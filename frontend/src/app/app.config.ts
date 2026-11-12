import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { errorInterceptor } from '@core/http/error.interceptor';
import { unwrapInterceptor } from '@core/http/unwrap.interceptor';
import { provideApiConfiguration } from '@api/api-configuration';
import { MatIconRegistry } from '@angular/material/icon';

// Font biểu tượng đang nạp trong index.html

const ICON_FONT_SET = 'material-symbols-outlined';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    // Thứ tự: errorInterceptor bọc ngoài, unwrapInterceptor gần backend nhất
    provideHttpClient(withInterceptors([errorInterceptor, unwrapInterceptor])),
    // Đường dẫn trong hợp đồng đã có tiền tố /api - gọi tương đối, cùng origin
    provideApiConfiguration(''),
    provideAppInitializer(() => {
      inject(MatIconRegistry).setDefaultFontSetClass(ICON_FONT_SET);
    }),
  ],
};
