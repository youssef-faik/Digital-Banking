import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, tap, switchMap, catchError, of } from 'rxjs';
import { LoginRequest, RegisterRequest, JwtResponse, AppUser, ChangePasswordRequest } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private tokenKey = 'jwt_token';
  private userKey = 'current_user';
  
  private currentUserSubject = new BehaviorSubject<AppUser | null>(this.getCurrentUser());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) {}  login(loginRequest: LoginRequest): Observable<AppUser> {
    return this.http.post<{token: string}>(`${this.apiUrl}/login`, loginRequest)
      .pipe(
        tap(response => {
          this.setToken(response.token);
        }),
        switchMap(() => {
          // After setting token, get user details
          return this.getMe().pipe(
            catchError((error) => {
              console.error('Error getting user details:', error);
              // Create a basic user object from token if getMe fails
              const basicUser: AppUser = {
                userId: 0,
                username: loginRequest.username,
                email: '',
                roles: []
              };
              this.setCurrentUser(basicUser);
              this.currentUserSubject.next(basicUser);
              return of(basicUser);
            })
          );
        })
      );
  }

  register(registerRequest: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, registerRequest);
  }
  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  getCurrentUser(): AppUser | null {
    const userStr = localStorage.getItem(this.userKey);
    return userStr ? JSON.parse(userStr) : null;
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  changePassword(changePasswordRequest: ChangePasswordRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/change-password`, changePasswordRequest);
  }
  getMe(): Observable<AppUser> {
    return this.http.get<AppUser>(`${this.apiUrl}/me`)
      .pipe(
        tap(user => {
          this.setCurrentUser(user);
          this.currentUserSubject.next(user);
        })
      );
  }

  updateProfile(profileData: { email: string }): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.apiUrl}/profile`, profileData)
      .pipe(
        tap(updatedUser => {
          this.setCurrentUser(updatedUser);
          this.currentUserSubject.next(updatedUser);
        })
      );
  }

  private setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  private setCurrentUser(user: AppUser): void {
    localStorage.setItem(this.userKey, JSON.stringify(user));
  }
}
