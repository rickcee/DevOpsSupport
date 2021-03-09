import { Observable, throwError } from 'rxjs';
import { HttpErrorResponse, HttpEvent, HttpHandler,HttpInterceptor, HttpRequest } from '@angular/common/http';

import { Injectable } from '@angular/core';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { AppService } from './app.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  constructor(private _appSvc : AppService, private _router: Router) { console.log('ErrorInterceptor - Constructor'); }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status == 401 && JSON.stringify(err).indexOf(this._appSvc.baseUserUrl) == -1) {
          console.log("401: " + JSON.stringify(err));
          // Handle 401 error
          this._router.navigateByUrl('/login');
        } else {
          return throwError(err);
        }
      })
    );
  }

}