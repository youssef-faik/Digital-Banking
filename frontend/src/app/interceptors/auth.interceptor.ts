import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Get the auth token from the service
  const authToken = authService.getToken();
  console.log('🔑 Auth Interceptor - Token:', authToken ? 'Present' : 'Missing');
  console.log('🌐 Request URL:', req.url);
  
  // Clone the request and add the authorization header if token exists
  let authReq = req;
  if (authToken && !req.url.includes('/auth/login') && !req.url.includes('/auth/register')) {
    authReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${authToken}`)
    });
    console.log('✅ Authorization header added to request');
  } else {
    console.log('❌ No authorization header added');
  }

  // Handle the request and catch errors
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        // Token is invalid or expired
        authService.logout();
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
