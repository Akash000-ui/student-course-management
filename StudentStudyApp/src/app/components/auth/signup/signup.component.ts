import { Component, OnInit, AfterViewInit, OnDestroy, Inject, PLATFORM_ID } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../../services/auth.service';
import { isPlatformBrowser, DOCUMENT } from '@angular/common';

declare const google: any;

@Component({
  selector: 'app-signup',
  standalone: false,
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})

export class SignupComponent implements OnInit, AfterViewInit, OnDestroy {
  signupForm!: FormGroup;
  otpForm!: FormGroup;
  loading = false;
  hidePassword = true;
  hideConfirmPassword = true;
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
    this.signupForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20)]],
      email: ['', [Validators.required, Validators.email]],
      mobileNumber: ['', [Validators.required, Validators.pattern('^[6-9]\\d{9}$')]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      agreeToTerms: [false, [Validators.requiredTrue]]
    }, { validators: this.passwordMatchValidator });

    this.otpForm = this.fb.group({
      otp: ['', [Validators.required, Validators.pattern('^\\d{6}$')]]
    });
  }

  // Custom validator for password confirmation
  private passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const password = control.get('password');
    const confirmPassword = control.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    return password.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  onSubmit(): void {
    if (this.signupForm.valid) {
      this.loading = true;
      this.errorMessage = '';
      const email = this.signupForm.value.email;

      // Send OTP to email
      this.authService.sendOtp(email).subscribe({
        next: (response) => {
          this.loading = false;
          if (response.success) {
            this.showOtpStep = true;
            this.otpSent = true;
            this.startResendTimer();
            this.snackBar.open('OTP sent to your email!', 'Close', {
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
    } else {
      this.markFormGroupTouched();
    }
  }

  onVerifyOtp(): void {
    if (this.otpForm.valid) {
      this.loading = true;
      this.errorMessage = '';
      const email = this.signupForm.value.email;
      const otp = this.otpForm.value.otp;

      // Verify OTP
      this.authService.verifyOtp(email, otp).subscribe({
        next: (response) => {
          if (response.success) {
            // OTP verified, now register the user
            this.registerUser();
          } else {
            this.loading = false;
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

  private registerUser(): void {
    const userData = {
      username: this.signupForm.value.username,
      email: this.signupForm.value.email,
      mobileNumber: this.signupForm.value.mobileNumber,
      password: this.signupForm.value.password
    };

    this.authService.register(userData).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.snackBar.open('Account created successfully!', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
          this.router.navigate(['/dashboard']);
        } else {
          this.errorMessage = response.message || 'Registration failed';
          this.snackBar.open(this.errorMessage, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'An error occurred during registration';
        this.snackBar.open(this.errorMessage, 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  onResendOtp(): void {
    if (this.resendDisabled) return;

    this.loading = true;
    const email = this.signupForm.value.email;

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

  goBackToSignup(): void {
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
    Object.keys(this.signupForm.controls).forEach(key => {
      const control = this.signupForm.get(key);
      control?.markAsTouched();
    });
  }

  private initializeGoogleButton(): void {
    try {
      if (!this.isBrowser || typeof google === 'undefined') return;
      const parent = this.document.getElementById('googleSignUpBtn');
      if (!parent) return;
      google.accounts.id.initialize({
        client_id: this.googleClientId,
        callback: (response: any) => this.onGoogleCredential(response)
      });
      google.accounts.id.renderButton(parent, {
        theme: 'outline',
        size: 'large',
        text: 'signup_with',
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
          this.snackBar.open('Signed up with Google', 'Close', { duration: 3000, panelClass: ['success-snackbar'] });
          this.router.navigate(['/dashboard']);
        } else {
          this.errorMessage = res.message || 'Google sign-up failed';
          this.snackBar.open(this.errorMessage, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Google sign-up error';
        this.snackBar.open(this.errorMessage, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
      }
    });
  }
}
