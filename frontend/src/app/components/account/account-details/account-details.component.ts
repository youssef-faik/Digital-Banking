import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject, takeUntil } from 'rxjs';

// Angular Material imports
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';

import { AccountService } from '../../../services/account.service';
import { OperationService } from '../../../services/operation.service';
import { BankAccount, AccountOperation } from '../../../models/bank.models';

@Component({
  selector: 'app-account-details',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTableModule,
    MatPaginatorModule
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
    private operationService: OperationService
  ) {}

  ngOnInit(): void {
    this.accountId = this.route.snapshot.params['id'];
    if (this.accountId) {
      this.loadAccountDetails();
    } else {
      alert('Account ID not provided');
      this.router.navigate(['/accounts']);
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAccountDetails(): void {
    this.loading = true;
    
    this.accountService.getAccount(this.accountId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (account) => {
          this.account = account;
          this.loadOperations();
        },
        error: (error) => {
          console.error('Error loading account details:', error);
          alert('Failed to load account details');
          this.loading = false;
        }
      });
  }  loadOperations(page: number = 0, size: number = 10): void {
    this.operationsLoading = true;
    this.pageIndex = page;
    this.pageSize = size;
    
    this.operationService.getAccountHistory(this.accountId, this.pageIndex, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          // Handle both array and paginated response
          if (Array.isArray(response)) {
            this.operations = response;
            this.totalElements = response.length;
          } else {
            this.operations = response.content || [];
            this.totalElements = response.totalElements || 0;
          }
          this.loading = false;
          this.operationsLoading = false;
        },
        error: (error) => {
          console.error('Error loading operations:', error);
          alert('Failed to load account operations');
          this.loading = false;
          this.operationsLoading = false;
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
    return this.account?.type === 'CURRENT';
  }
  
  isSavingAccount(): boolean {
    return this.account?.type === 'SAVING';
  }
  
  getCurrentAccount(): any {
    return this.isCurrentAccount() ? this.account : null;
  }
  
  getSavingAccount(): any {
    return this.isSavingAccount() ? this.account : null;
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
    this.loadOperations();
  }

  refreshData(): void {
    this.loadAccountDetails();
  }

  editAccount(): void {
    this.router.navigate(['/accounts', 'edit', this.accountId]);
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
