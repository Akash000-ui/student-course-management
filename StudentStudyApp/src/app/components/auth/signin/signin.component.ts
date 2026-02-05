import { Component, OnInit, Inject, PLATFORM_ID, AfterViewInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../services/auth.service';
import { isPlatformBrowser, DOCUMENT } from '@angular/common';

declare const google: any;

@Component({
  selector: 'app-signin',
  standalone: false,
  templateUrl: './signin.component.html',
  styleUrl: './signin.component.css'
})

export class SigninComponent implements OnInit, AfterViewInit, OnDestroy {
  signinForm!: FormGroup;
  otpForm!: FormGroup;
  loading = false;
  hidePassword = true;
  errorMessage = '';
  showOtpStep = false;
  otpSent = false;
  resendDisabled = false;
  resendCountdown = 0;
  private isBrowser: boolean;
  private googleClientId = '1066899334085-pk107ga4netakv1f4j1lv51sd463aiab.apps.googleusercontent.com';
  private resendTimer: any;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar,
    @Inject(PLATFORM_ID) platformId: Object,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    this.initializeForm();
  }

  ngAfterViewInit(): void {
    if (!this.isBrowser) return;
    // Load Google Identity Services script if not already loaded
    if (!this.document.getElementById('google-identity')) {
      const script = this.document.createElement('script');
      script.id = 'google-identity';
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = () => this.initializeGoogleButton();
      this.document.head.appendChild(script);
    } else {
      this.initializeGoogleButton();
    }
  }

  private initializeForm(): void {
    this.signinForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]],
      rememberMe: [false]
    });

    this.otpForm = this.fb.group({
      otp: ['', [Validators.required, Validators.pattern('^\\d{6}$')]]
    });
  }

  onSubmit(): void {
    if (this.signinForm.valid) {
      this.loading = true;
      this.errorMessage = '';

      // 2FA Flow: Step 1 - Verify password WITHOUT logging in
      const email = this.signinForm.value.email;
      const password = this.signinForm.value.password;

      this.authService.verifyPassword(email, password).subscribe({
        next: (response) => {
          if (response.success) {
            // Password correct, now send OTP for 2FA
            this.loading = false;
            this.sendOtpForVerification();
          } else {
            this.loading = false;
            this.errorMessage = response.message || 'Invalid credentials';
            this.snackBar.open(this.errorMessage, 'Close', {
              duration: 3000,
              panelClass: ['error-snackbar']
            });
          }
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = error.error?.message || 'An error occurred during sign in';
          this.snackBar.open(this.errorMessage, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    } else {
      this.markFormGroupTouched();
    }
  }

  private sendOtpForVerification(): void {
    this.loading = true;
    const email = this.signinForm.value.email;

    this.authService.sendOtp(email).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.showOtpStep = true;
          this.otpSent = true;
          this.startResendTimer();
          this.snackBar.open('OTP sent to your email! Please verify to continue.', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
        } else {
          this.errorMessage = response.message || 'Failed to send OTP';
          this.snackBar.open(this.errorMessage, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'An error occurred while sending OTP';
        this.snackBar.open(this.errorMessage, 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onVerifyOtp(): void {
    if (this.otpForm.valid) {
      this.loading = true;
      this.errorMessage = '';
      const email = this.signinForm.value.email;
      const otp = this.otpForm.value.otp;

      // 2FA Flow: Complete login after password + OTP verification
      this.authService.completeLogin(email, otp).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.success) {
            this.snackBar.open('Sign in successful!', 'Close', {
              duration: 3000,
              panelClass: ['success-snackbar']
            });
            this.router.navigate(['/dashboard']);
          } else {
            this.errorMessage = response.message || 'Invalid OTP';
            this.snackBar.open(this.errorMessage, 'Close', {
              duration: 3000,
              panelClass: ['error-snackbar']
            });
          }
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = error.error?.message || 'OTP verification failed';
          this.snackBar.open(this.errorMessage, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
    }
  }

  onResendOtp(): void {
    if (this.resendDisabled) return;

    this.loading = true;
    const email = this.signinForm.value.email;

    this.authService.resendOtp(email).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.startResendTimer();
          this.snackBar.open('OTP resent successfully!', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
        } else {
          this.snackBar.open(response.message || 'Failed to resend OTP', 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      },
      error: (error) => {
        this.loading = false;
        this.snackBar.open(error.error?.message || 'Error resending OTP', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  private startResendTimer(): void {
    this.resendDisabled = true;
    this.resendCountdown = 60;

    this.resendTimer = setInterval(() => {
      this.resendCountdown--;
      if (this.resendCountdown <= 0) {
        this.resendDisabled = false;
        clearInterval(this.resendTimer);
      }
    }, 1000);
  }

  goBackToSignin(): void {
    this.showOtpStep = false;
    this.otpForm.reset();
    if (this.resendTimer) {
      clearInterval(this.resendTimer);
    }
    this.resendDisabled = false;
    this.resendCountdown = 0;
  }

  ngOnDestroy(): void {
    if (this.resendTimer) {
      clearInterval(this.resendTimer);
    }
  }

  private markFormGroupTouched(): void {
    Object.keys(this.signinForm.controls).forEach(key => {
      const control = this.signinForm.get(key);
      control?.markAsTouched();
    });
  }

  private initializeGoogleButton(): void {
    try {
      if (!this.isBrowser || typeof google === 'undefined') return;
      const parent = this.document.getElementById('googleSignInBtn');
      if (!parent) return;
      // Render the Google button
      google.accounts.id.initialize({
        client_id: this.googleClientId,
        callback: (response: any) => this.onGoogleCredential(response)
      });
      google.accounts.id.renderButton(parent, {
        theme: 'outline',
        size: 'large',
        text: 'signin_with',
        shape: 'rectangular',
        width: 320
      });
    } catch (e) {
      console.error('Failed to init Google button', e);
    }
  }

  private onGoogleCredential(resp: any): void {
    const idToken = resp?.credential;
    if (!idToken) return;
    this.loading = true;
    this.authService.googleLogin(idToken, this.googleClientId).subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.snackBar.open('Signed in with Google', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
          this.router.navigate(['/dashboard']);
        } else {
          this.errorMessage = res.message || 'Google sign-in failed';
          this.snackBar.open(this.errorMessage, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Google sign-in error';
        this.snackBar.open(this.errorMessage, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }
}
