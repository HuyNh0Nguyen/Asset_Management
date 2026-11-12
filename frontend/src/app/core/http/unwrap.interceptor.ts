import { HttpInterceptorFn, HttpResponse } from "@angular/common/http";
import { map } from "rxjs";
import { isApiEnvelope } from "./api-envelope";

// Bóc phong bì thành công: service sinh tự dộng nhận thẳng DTO trong "data"
export const unwrapInterceptor: HttpInterceptorFn = (req, next) =>
    next(req).pipe(
        map((event) => 
            event instanceof HttpResponse && isApiEnvelope(event.body) ? event.clone({ body: event.body.data }) : event,
        )   
    );