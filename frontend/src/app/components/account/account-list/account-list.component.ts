import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterModule } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { AccountService } from '../../../services/account.service';
import { BankAccount } from '../../../models/bank.models';

@Component({
  selector: 'app-account-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatChipsModule,
    MatTooltipModule,
    MatSnackBarModule,
    RouterModule,
    ReactiveFormsModule
  ],
  templateUrl: './account-list.component.html',
  styleUrl: './account-list.component.scss'
})
export class AccountListComponent implements OnInit {
  private accountService = inject(AccountService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  accounts: BankAccount[] = [];
  displayedColumns: string[] = ['id', 'customer', 'type', 'balance', 'status', 'createdAt', 'actions'];
  isLoading = false;
  searchControl = new FormControl('');
  typeFilter = new FormControl('');
  statusFilter = new FormControl('');
  filteredAccounts: BankAccount[] = [];

  ngOnInit() {
    this.loadAccounts();
    this.setupFilters();
  }
  setupFilters() {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(() => this.applyFilters());

    this.typeFilter.valueChanges.subscribe(() => this.applyFilters());
    this.statusFilter.valueChanges.subscribe(() => this.applyFilters());
  }

  loadAccounts() {
    this.isLoading = true;
    this.accountService.getAllAccounts().subscribe({
      next: (accounts: BankAccount[]) => {
        this.accounts = accounts;
        this.filteredAccounts = accounts;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        this.snackBar.open(
          'Failed to load accounts. Please try again.',
          'Close',
          { duration: 5000, panelClass: 'error-snackbar' }
        );
      }
    });
  }
  applyFilters() {
    let filtered = this.accounts;

    // Apply search filter
    const searchTerm = this.searchControl.value?.toLowerCase() || '';
    if (searchTerm) {
      filtered = filtered.filter(account =>
        account.id.toLowerCase().includes(searchTerm) ||
        // Use safe navigation for customer name and email
        account.customer?.name?.toLowerCase().includes(searchTerm) ||
        account.customer?.email?.toLowerCase().includes(searchTerm) ||
        (account.status && account.status.toLowerCase().includes(searchTerm)) ||
        (account.type && account.type.toLowerCase().includes(searchTerm))
      );
    }

    // Apply type filter
    const typeFilter = this.typeFilter.value;
    if (typeFilter) {
      filtered = filtered.filter(account => account.type === typeFilter);
    }

    // Apply status filter
    const statusFilter = this.statusFilter.value;
    if (statusFilter) {
      filtered = filtered.filter(account => account.status === statusFilter);
    }

    this.filteredAccounts = filtered;
  }

  addAccount() {
    this.router.navigate(['/accounts/new']);
  }

  viewAccount(account: BankAccount) {
    this.router.navigate(['/accounts', account.id]);
  }
  editAccount(account: BankAccount) {
    this.router.navigate(['/accounts/edit', account.id]);
  }

  deleteAccount(account: BankAccount) {
    if (confirm(`Are you sure you want to delete account ${account.id}? This action cannot be undone.`)) {
      this.accountService.deleteAccount(account.id).subscribe({
        next: () => {
          this.snackBar.open(
            'Account deleted successfully',
            'Close',
            { duration: 3000, panelClass: 'success-snackbar' }
          );
          this.loadAccounts(); // Refresh the list
        },
        error: (error) => {
          console.error('Error deleting account:', error);
          this.snackBar.open(
            'Failed to delete account. Please try again.',
            'Close',
            { duration: 5000, panelClass: 'error-snackbar' }
          );
        }
      });
    }
  }

  toggleAccountStatus(account: BankAccount) {
    const newStatus = account.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    const action = newStatus === 'ACTIVE' ? 'activate' : 'suspend';
    
    if (confirm(`Are you sure you want to ${action} account ${account.id}?`)) {
      this.accountService.toggleAccountStatus(account.id, newStatus).subscribe({
        next: () => {
          this.snackBar.open(
            `Account ${action}d successfully`,
            'Close',
            { duration: 3000, panelClass: 'success-snackbar' }
          );
          this.loadAccounts(); // Refresh the list
        },
        error: (error) => {
          console.error('Error updating account status:', error);
          this.snackBar.open(
            `Failed to ${action} account. Please try again.`,
            'Close',
            { duration: 5000, panelClass: 'error-snackbar' }
          );
        }
      });
    }
  }

  formatBalance(balance: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(balance);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }
  getAccountTypeColor(type: string): string {
    return type === 'CURRENT' ? 'primary' : 'accent';
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'primary';
      case 'SUSPENDED': return 'warn';
      default: return 'accent';
    }
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case 'ACTIVE': return 'check_circle';
      case 'SUSPENDED': return 'pause_circle';
      default: return 'help';
    }
  }
}
