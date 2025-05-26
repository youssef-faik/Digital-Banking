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
import { MatPaginatorModule } from '@angular/material/paginator';
import { Router, RouterModule } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { AccountService } from '../../../services/account.service';
import { BankAccount } from '../../../models/bank.models';

@Component({
  selector: 'app-account-list',
  standalone: true,  imports: [
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
    MatPaginatorModule,
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
  
  // Pagination properties
  pageSize = 10;
  pageIndex = 0;
  totalElements = 0;
  pageSizeOptions = [5, 10, 25, 50];
  
  // Current filters for server-side filtering
  currentSearchTerm = '';
  currentTypeFilter = '';
  currentStatusFilter = '';

  ngOnInit() {
    this.loadAccounts();
    this.setupFilters();
  }  setupFilters() {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(() => {
        this.pageIndex = 0; // Reset to first page when filtering
        this.applyFilters();
      });

    this.typeFilter.valueChanges.subscribe(() => {
      this.pageIndex = 0; // Reset to first page when filtering
      this.applyFilters();
    });
    
    this.statusFilter.valueChanges.subscribe(() => {
      this.pageIndex = 0; // Reset to first page when filtering
      this.applyFilters();
    });
  }
  loadAccounts() {
    this.isLoading = true;
    
    // Use pagination parameters when calling getAllAccounts
    this.accountService.getAllAccounts(this.pageIndex, this.pageSize).subscribe({
      next: (response: any) => {
        // Handle both paginated and non-paginated responses
        if (response.content) {
          // Paginated response
          this.accounts = response.content;
          this.totalElements = response.totalElements;
        } else {
          // Non-paginated response (fallback)
          this.accounts = response;
          this.totalElements = response.length;
        }
        this.applyClientSideFilters();
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
    // Update current filter values
    this.currentSearchTerm = this.searchControl.value || '';
    this.currentTypeFilter = this.typeFilter.value || '';
    this.currentStatusFilter = this.statusFilter.value || '';
    
    // For now, reload data from server and apply client-side filtering
    // TODO: Implement server-side filtering when backend supports it
    this.loadAccounts();
  }

  applyClientSideFilters() {
    let filtered = this.accounts;

    // Apply search filter
    if (this.currentSearchTerm) {
      const searchTerm = this.currentSearchTerm.toLowerCase();
      filtered = filtered.filter(account =>
        account.id.toLowerCase().includes(searchTerm) ||
        account.customer?.name?.toLowerCase().includes(searchTerm) ||
        account.customer?.email?.toLowerCase().includes(searchTerm) ||
        (account.status && account.status.toLowerCase().includes(searchTerm)) ||
        (account.type && account.type.toLowerCase().includes(searchTerm))
      );
    }

    // Apply type filter
    if (this.currentTypeFilter) {
      filtered = filtered.filter(account => account.type === this.currentTypeFilter);
    }

    // Apply status filter
    if (this.currentStatusFilter) {
      filtered = filtered.filter(account => account.status === this.currentStatusFilter);
    }

    this.filteredAccounts = filtered;
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAccounts();
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
        next: () => {          this.snackBar.open(
            'Account deleted successfully',
            'Close',
            { duration: 3000, panelClass: 'success-snackbar' }
          );
          this.loadAccounts(); // Reload current page// Refresh the list
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
        next: () => {          this.snackBar.open(
            `Account ${action}d successfully`,
            'Close',
            { duration: 3000, panelClass: 'success-snackbar' }
          );
          this.loadAccounts(); // Refresh the current page
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
