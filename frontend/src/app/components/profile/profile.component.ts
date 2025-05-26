import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { MatGridListModule } from '@angular/material/grid-list';

import { AuthService } from '../../services/auth.service';
import { DashboardService, DashboardStatsDTO } from '../../services/dashboard.service';
import { AppUser } from '../../models/auth.models';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatDividerModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    MatGridListModule
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  currentUser: AppUser | null = null;
  dashboardStats: DashboardStatsDTO | null = null;
  profileForm: FormGroup;
  isLoading = true;
  isEditing = false;
  isSaving = false;

  constructor(
    private authService: AuthService,
    private dashboardService: DashboardService,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.profileForm = this.formBuilder.group({
      username: [{ value: '', disabled: true }],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit(): void {
    this.loadProfileData();
  }

  private loadProfileData(): void {
    this.isLoading = true;
    
    // Load current user information
    this.authService.getMe().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.populateForm();
        this.loadDashboardStats();
      },
      error: (error) => {
        console.error('Error loading user profile:', error);
        this.snackBar.open('Error loading profile information', 'Close', {
          duration: 3000,
          panelClass: ['error-snackbar']
        });
        this.isLoading = false;
      }
    });
  }

  private populateForm(): void {
    if (this.currentUser) {
      this.profileForm.patchValue({
        username: this.currentUser.username,
        email: this.currentUser.email
      });
    }
  }

  private loadDashboardStats(): void {
    this.dashboardService.getDashboardStats().subscribe({
      next: (stats) => {
        this.dashboardStats = stats;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error loading dashboard stats:', error);
        this.isLoading = false;
      }
    });
  }

  toggleEdit(): void {
    if (this.isEditing) {
      // Cancel editing - reset form
      this.populateForm();
      this.isEditing = false;
    } else {
      // Start editing
      this.isEditing = true;
    }
  }
  saveProfile(): void {
    if (this.profileForm.valid && this.currentUser) {
      this.isSaving = true;
      const updatedEmail = this.profileForm.get('email')?.value;
      
      // Call the actual backend API to update profile
      this.authService.updateProfile({ email: updatedEmail }).subscribe({
        next: (updatedUser) => {
          this.currentUser = updatedUser;
          this.isEditing = false;
          this.isSaving = false;
          this.snackBar.open('Profile updated successfully', 'Close', {
            duration: 3000,
            panelClass: ['success-snackbar']
          });
        },
        error: (error) => {
          this.isSaving = false;
          console.error('Error updating profile:', error);
          this.snackBar.open(
            error.error?.message || 'Error updating profile. Please try again.',
            'Close',
            {
              duration: 5000,
              panelClass: ['error-snackbar']
            }
          );
        }
      });
    }
  }

  getRoleChipColor(role: string): string {
    switch (role.toUpperCase()) {
      case 'ADMIN':
        return 'warn';
      case 'USER':
        return 'primary';
      default:
        return 'accent';
    }
  }

  getInitials(name?: string): string {
    if (!name) return 'U';
    return name.split(' ')
      .map(word => word.charAt(0))
      .join('')
      .toUpperCase()
      .substring(0, 2);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }
}
