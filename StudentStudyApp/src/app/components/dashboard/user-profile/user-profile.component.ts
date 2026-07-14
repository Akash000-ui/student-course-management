import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { UserService, UserProfile, UpdateProfileRequest } from '../../../services/user.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-user-profile',
  standalone: false,
  templateUrl: './user-profile.component.html',
  styleUrl: './user-profile.component.css'
})
export class UserProfileComponent implements OnInit {
  profileForm!: FormGroup;
  passwordForm!: FormGroup;
  loading = false;
  savingProfile = false;
  savingPassword = false;
  userProfile: UserProfile | null = null;
  showPasswordSection = false;
  hideCurrentPassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.initializeForms();
    this.loadUserProfile();
  }

  initializeForms(): void {
    this.profileForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50)]],
      email: [{ value: '', disabled: true }],
      mobileNumber: ['', [Validators.pattern(/^[0-9]{10}$/)]]
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required, Validators.minLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup): { [key: string]: boolean } | null {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { passwordMismatch: true };
  }

  loadUserProfile(): void {
    this.loading = true;
    this.userService.getUserProfile().subscribe({
      next: (response) => {
        if (response.success && response.data) {
          this.userProfile = response.data;
          this.profileForm.patchValue({
            username: response.data.username,
            email: response.data.email,
            mobileNumber: response.data.mobileNumber || ''
          });
        } else {
          this.showError(response.message || 'Failed to load profile');
        }
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading profile:', error);
        this.showError('Failed to load profile');
        this.loading = false;
      }
    });
  }

  onUpdateProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.savingProfile = true;
    const request: UpdateProfileRequest = {
      username: this.profileForm.value.username,
      mobileNumber: this.profileForm.value.mobileNumber || undefined
    };

    this.userService.updateUserProfile(request).subscribe({
      next: (response) => {
        this.savingProfile = false;
        if (response.success) {
          this.showSuccess('Profile updated successfully');
          if (response.data) {
            this.userProfile = response.data;
            // Update local storage user data
            const currentUser = this.authService.getCurrentUser();
            if (currentUser) {
              currentUser.username = response.data.username;
              localStorage.setItem('user', JSON.stringify(currentUser));
            }
          }
        } else {
          this.showError(response.message || 'Failed to update profile');
        }
      },
      error: (error) => {
        this.savingProfile = false;
        console.error('Error updating profile:', error);
        this.showError(error.error?.message || 'Failed to update profile');
      }
    });
  }

  onChangePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    this.savingPassword = true;
    const request: UpdateProfileRequest = {
      currentPassword: this.passwordForm.value.currentPassword,
      newPassword: this.passwordForm.value.newPassword
    };

    this.userService.updateUserProfile(request).subscribe({
      next: (response) => {
        this.savingPassword = false;
        if (response.success) {
          this.showSuccess('Password changed successfully');
          this.passwordForm.reset();
          this.showPasswordSection = false;
        } else {
          this.showError(response.message || 'Failed to change password');
        }
      },
      error: (error) => {
        this.savingPassword = false;
        console.error('Error changing password:', error);
        this.showError(error.error?.message || 'Failed to change password');
      }
    });
  }

  togglePasswordSection(): void {
    this.showPasswordSection = !this.showPasswordSection;
    if (!this.showPasswordSection) {
      this.passwordForm.reset();
    }
  }

  goBack(): void {
    this.router.navigate(['/dashboard']);
  }

  getRoleBadgeClass(role: string): string {
    return role === 'ADMIN' ? 'role-badge-admin' : 'role-badge-user';
  }

  showSuccess(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });
  }

  showError(message: string): void {
    this.snackBar.open(message, 'Close', {
      duration: 3000,
      panelClass: ['error-snackbar']
    });
  }
}
