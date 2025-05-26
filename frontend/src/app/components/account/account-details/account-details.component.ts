import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';
import { filter, map } from 'rxjs/operators';

// Angular Material imports
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { AccountService } from '../../../services/account.service';
import { OperationService } from '../../../services/operation.service';
import { BankAccount, CurrentAccount, SavingAccount, AccountOperation, AccountHistoryDTO } from '../../../models/bank.models';

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,    MatProgressSpinnerModule,
    MatChipsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSnackBarModule
  ],
  templateUrl: './account-details.component.html',
  styleUrl: './account-details.component.scss'
})
export class AccountDetailsComponent implements OnInit, OnDestroy {
  private destroy$ = new Subject<void>();
    account: BankAccount | null = null;
  operations: AccountOperation[] = [];
  loading = true;
  operationsLoading = false;
  accountId: string = '';
  
  // Properties for table pagination
  pageSize = 10;
  pageIndex = 0;
  totalElements = 0;
  
  // Display columns for operations table
  displayedColumns = ['date', 'type', 'amount', 'description', 'balance'];
  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private accountService: AccountService,
    private operationService: OperationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    console.log('🚀 AccountDetailsComponent ngOnInit called');
    this.route.paramMap
      .pipe(
        filter(params => params.has('id')),
        map(params => params.get('id')!),
        takeUntil(this.destroy$)
      )
      .subscribe({
        next: (id) => {
          console.log('📋 Account ID from route:', id);
          this.accountId = id;
          this.loadAccountDetails();
        },
        error: (error) => {
          console.error('❌ Error getting account ID from route:', error);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadAccountDetails(): void {
    console.log('🔄 loadAccountDetails called for account:', this.accountId);
    if (!this.accountId) {
      console.warn('⚠️ No account ID available');
      return;
    }

    this.loading = true;
    this.accountService.getAccount(this.accountId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({        next: (account) => {
          console.log('✅ Account loaded successfully:', account);
          console.log('🔍 Account type:', account?.type);
          console.log('🔍 Account overdraft:', account?.overdraft);
          console.log('🔍 Account interestRate:', account?.interestRate);
          this.account = account;
          this.loading = false;
          this.loadOperations(0, this.pageSize);
        },
        error: (error) => {
          console.error('❌ Error loading account details:', error);
          this.loading = false;
          this.snackBar.open('Failed to load account details', 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }

  public loadOperations(page: number = 0, size: number = 10): void {
    console.log('🔄 loadOperations called with page:', page, 'size:', size, 'accountId:', this.accountId);
    if (!this.accountId) {
      console.warn('⚠️ Cannot load operations: No account ID');
      return;
    }

    this.operationsLoading = true;
    this.pageIndex = page;
    this.pageSize = size;
    
    console.log('🌐 Making API call to get account history...');
    this.operationService.getAccountHistory(this.accountId, this.pageIndex, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          console.log('✅ Operations loaded successfully:', response);
          // Handle AccountHistoryDTO response
          this.operations = response.accountOperationDTOS || [];
          this.totalElements = response.totalPages * response.pageSize || 0;
          this.loading = false;
          this.operationsLoading = false;
          console.log('📊 Operations count:', this.operations.length);
        },
        error: (error) => {
          console.error('❌ Error loading operations:', error);
          this.operations = [];
          this.totalElements = 0;
          this.loading = false;
          this.operationsLoading = false;
          
          // Show user-friendly error message
          this.snackBar.open('Failed to load account operations', 'Close', {
            duration: 5000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }

  goBack(): void {
    this.router.navigate(['/accounts']);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }
  formatDate(date: string): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
  // Additional methods needed by template
  isCurrentAccount(): boolean {
    const result = this.account?.type === 'CURRENT';
    console.log('🔍 isCurrentAccount:', result, 'account type:', this.account?.type);
    return result;
  }
  
  isSavingAccount(): boolean {
    const result = this.account?.type === 'SAVING';
    console.log('🔍 isSavingAccount:', result, 'account type:', this.account?.type);
    return result;
  }
    getCurrentAccount(): CurrentAccount | null {
    if (this.isCurrentAccount() && this.account) {
      return this.account as CurrentAccount;
    }
    return null;
  }
  
  getSavingAccount(): SavingAccount | null {
    if (this.isSavingAccount() && this.account) {
      return this.account as SavingAccount;
    }
    return null;
  }
  
  navigateToOperation(type: string): void {
    this.router.navigate(['/operations', type], {
      queryParams: { accountId: this.accountId }
    });
  }
  
  getOperationTypeColor(type: string): string {
    switch (type?.toUpperCase()) {
      case 'CREDIT':
        return 'primary';
      case 'DEBIT':
        return 'warn';
      case 'TRANSFER':
        return 'accent';
      default:
        return 'primary';
    }
  }
  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadOperations(this.pageIndex, this.pageSize);
  }refreshData(): void {
    this.loadAccountDetails();
  }

  editAccount(): void {
    this.router.navigate(['/accounts', 'edit', this.accountId]);
  }

  deleteAccount(): void {
    if (confirm(`Are you sure you want to delete account ${this.accountId}? This action cannot be undone.`)) {
      this.accountService.deleteAccount(this.accountId).subscribe({
        next: () => {
          this.snackBar.open(
            'Account deleted successfully',
            'Close',
            { duration: 3000, panelClass: 'success-snackbar' }
          );
          this.router.navigate(['/accounts']);
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

  toggleAccountStatus(): void {
    if (!this.account) return;
    
    const newStatus = this.account.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE';
    const action = newStatus === 'ACTIVE' ? 'activate' : 'suspend';
    
    if (confirm(`Are you sure you want to ${action} this account?`)) {
      this.accountService.toggleAccountStatus(this.accountId, newStatus).subscribe({
        next: (updatedAccount) => {
          this.account = updatedAccount;
          this.snackBar.open(
            `Account ${action}d successfully`,
            'Close',
            { duration: 3000, panelClass: 'success-snackbar' }
          );
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

  getOperationIcon(type: string): string {
    switch (type?.toUpperCase()) {
      case 'CREDIT':
        return 'add_circle';
      case 'DEBIT':
        return 'remove_circle';
      case 'TRANSFER':
        return 'swap_horiz';
      default:
        return 'account_balance_wallet';
    }
  }
}
