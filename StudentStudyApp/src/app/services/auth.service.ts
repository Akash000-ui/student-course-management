import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { isPlatformBrowser } from '@angular/common';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  username: string;
  mobileNumber: string;
  password: string;
}

export interface AuthResponse {
  success: boolean;
  message: string;
  data?: {
    token: string;
    user: {
      id: string;
      email: string;
      username: string;
      roles: string[];
    };
  };
  statusCode: number;
}

export interface OtpResponse {
  success: boolean;
  message: string;
  data?: boolean;
  statusCode: number;
}

export interface SimpleApiResponse {
  success: boolean;
  message: string;
  data?: any;
  statusCode: number;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = "http://localhost:8080/api/auth";
  private isLoggedInSubject = new BehaviorSubject<boolean>(false);
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();
  private isBrowser: boolean;

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
    // Initialize authentication state immediately
    this.initializeAuthState();
  }

  private initializeAuthState(): void {
    if (this.isBrowser) {
      const hasToken = this.hasToken();
      // Only set to true if we actually have a valid token
      this.isLoggedInSubject.next(hasToken);
    } else {
      // For server-side rendering, always start as false
      this.isLoggedInSubject.next(false);
    }
  }

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, credentials)
      .pipe(
        tap(response => {
          if (response.success && response.data?.token && this.isBrowser) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.user));
            this.isLoggedInSubject.next(true);
          }
        })
      );
  }

  register(userData: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, userData)
      .pipe(
        tap(response => {
          if (response.success && response.data?.token && this.isBrowser) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.user));
            this.isLoggedInSubject.next(true);
          }
        })
      );
  }

  // Google OAuth: verify Google ID token on backend and receive our JWT
  googleLogin(idToken: string, clientId?: string): Observable<AuthResponse> {
    const payload: any = { idToken };
    if (clientId) payload.clientId = clientId;
    return this.http.post<AuthResponse>(`${this.baseUrl}/google`, payload)
      .pipe(
        tap(response => {
          if (response.success && response.data?.token && this.isBrowser) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.user));
            this.isLoggedInSubject.next(true);
          }
        })
      );
  }

  // OTP Methods
  sendOtp(email: string): Observable<OtpResponse> {
    return this.http.post<OtpResponse>('http://localhost:8080/api/otp/send', { email });
  }

  verifyOtp(email: string, otp: string): Observable<OtpResponse> {
    return this.http.post<OtpResponse>('http://localhost:8080/api/otp/verify', { email, otp });
  }

  resendOtp(email: string): Observable<OtpResponse> {
    return this.http.post<OtpResponse>('http://localhost:8080/api/otp/resend', { email });
  }

  loginWithOtp(email: string, otp: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login-otp`, { email, otp })
      .pipe(
        tap(response => {
          if (response.success && response.data?.token && this.isBrowser) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.user));
            this.isLoggedInSubject.next(true);
          }
        })
      );
  }

  // 2FA Methods - Password verification + OTP
  verifyPassword(email: string, password: string): Observable<SimpleApiResponse> {
    return this.http.post<SimpleApiResponse>(`${this.baseUrl}/verify-password`, { email, password });
  }

  completeLogin(email: string, otp: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/complete-login`, { email, otp })
      .pipe(
        tap(response => {
          if (response.success && response.data?.token && this.isBrowser) {
            localStorage.setItem('token', response.data.token);
            localStorage.setItem('user', JSON.stringify(response.data.user));
            this.isLoggedInSubject.next(true);
          }
        })
      );
  }

  logout(): void {
    if (this.isBrowser) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
    this.isLoggedInSubject.next(false);
  }

  getToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    return localStorage.getItem('token');
  }

  getCurrentUser(): any {
    if (!this.isBrowser) {
      return null;
    }
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  private hasToken(): boolean {
    if (!this.isBrowser) {
      return false;
    }
    const token = localStorage.getItem('token');
    if (!token) {
      return false;
    }

    // Basic token validation - check if it's not expired or malformed
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Date.now() / 1000;
      if (payload.exp && payload.exp < now) {
        // Token expired, remove it
        this.logout();
        return false;
      }
      return true;
    } catch (error) {
      // Invalid token format, remove it
      this.logout();
      return false;
    }
  }

  isAuthenticated(): boolean {
    return this.hasToken();
  }

  isAdmin(): boolean {
    if (!this.isBrowser) {
      return false;
    }
    const user = this.getCurrentUser();
    return user && user.roles && user.roles.includes('ADMIN');
  }

  forgotPassword(email: string): Observable<SimpleApiResponse> {
    return this.http.post<SimpleApiResponse>(`${this.baseUrl}/forgot-password`, { email });
  }

  verifyResetOtp(data: { email: string; otp: string }): Observable<SimpleApiResponse> {
    return this.http.post<SimpleApiResponse>(`${this.baseUrl}/verify-reset-otp`, data);
  }

  resetPassword(data: { email: string; otp: string; newPassword: string }): Observable<SimpleApiResponse> {
    return this.http.post<SimpleApiResponse>(`${this.baseUrl}/reset-password`, data);
  }
}
