import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: false,
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  currentStep: 'email' | 'otp' | 'reset' = 'email';

  emailForm: FormGroup;
  otpForm: FormGroup;
  resetForm: FormGroup;

  loading = false;
  errorMessage = '';
  successMessage = '';
  email = '';

  showPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.emailForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.otpForm = this.fb.group({
      otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]]
    });

    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { 'mismatch': true };
  }

  onSubmitEmail() {
    if (this.emailForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.email = this.emailForm.value.email;

    this.authService.forgotPassword(this.email).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.successMessage = response.message || 'OTP sent successfully!';
          setTimeout(() => {
            this.currentStep = 'otp';
            this.successMessage = '';
          }, 1500);
        } else {
          this.errorMessage = response.message || 'Failed to send OTP';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'An error occurred. Please try again.';
      }
    });
  }

  onSubmitOtp() {
    if (this.otpForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const otpData = {
      email: this.email,
      otp: this.otpForm.value.otp
    };

    this.authService.verifyResetOtp(otpData).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.successMessage = response.message || 'OTP verified successfully!';
          setTimeout(() => {
            this.currentStep = 'reset';
            this.successMessage = '';
          }, 1500);
        } else {
          this.errorMessage = response.message || 'Invalid OTP';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'OTP verification failed';
      }
    });
  }

  onSubmitReset() {
    if (this.resetForm.invalid) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const resetData = {
      email: this.email,
      otp: this.otpForm.value.otp,
      newPassword: this.resetForm.value.newPassword
    };

    this.authService.resetPassword(resetData).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.success) {
          this.successMessage = response.message || 'Password reset successfully!';
          setTimeout(() => {
            this.router.navigate(['/signin']);
          }, 2000);
        } else {
          this.errorMessage = response.message || 'Password reset failed';
        }
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.error?.message || 'An error occurred';
      }
    });
  }

  proceedToDashboard() {
    // First reset password, then navigate
    this.onSubmitReset();
  }

  navigateToSignIn() {
    this.router.navigate(['/signin']);
  }

  togglePasswordVisibility(field: 'password' | 'confirm') {
    if (field === 'password') {
      this.showPassword = !this.showPassword;
    } else {
      this.showConfirmPassword = !this.showConfirmPassword;
    }
  }
}
