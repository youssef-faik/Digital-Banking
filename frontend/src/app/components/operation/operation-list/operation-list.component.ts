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
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterModule } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { OperationService } from '../../../services/operation.service';
import { AccountOperation, Page, AccountOperationDTO } from '../../../models/bank.models';

@Component({
  selector: 'app-operation-list',
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
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    RouterModule,
    ReactiveFormsModule
  ],
  templateUrl: './operation-list.component.html',
  styleUrls: ['./operation-list.component.scss']
})
export class OperationListComponent implements OnInit {
  private operationService = inject(OperationService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  operations: AccountOperationDTO[] = [];
  displayedColumns: string[] = ['date', 'type', 'accountId', 'amount', 'description', 'actions'];
  isLoading = false;
  searchControl = new FormControl('');
  typeFilter = new FormControl('');
  filteredOperations: AccountOperationDTO[] = [];

  // Pagination
  pageSize = 10;
  pageIndex = 0;
  totalElements = 0;

  ngOnInit() {
    this.loadOperations();
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
  }
  loadOperations() {
    this.isLoading = true;
    this.operationService.getAllOperations(this.pageIndex, this.pageSize).subscribe({
      next: (page: Page<AccountOperationDTO>) => {
        this.operations = page.content;
        this.filteredOperations = page.content;
        this.totalElements = page.totalElements;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        this.snackBar.open(
          'Failed to load operations. Please try again.',
          'Close',
          { duration: 5000, panelClass: 'error-snackbar' }
        );
        console.error('Error loading operations:', error);
      }
    });
  }
  applyFilters() {
    let filtered = this.operations;

    // Apply search filter
    const searchTerm = this.searchControl.value?.toLowerCase() || '';
    if (searchTerm) {
      filtered = filtered.filter(operation =>
        operation.bankAccountId?.toLowerCase().includes(searchTerm) ||
        operation.description?.toLowerCase().includes(searchTerm) ||
        operation.type?.toString().toLowerCase().includes(searchTerm)
      );
    }

    // Apply type filter
    const typeFilter = this.typeFilter.value;
    if (typeFilter) {
      filtered = filtered.filter(operation => operation.type === typeFilter);
    }

    this.filteredOperations = filtered;
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadOperations();
  }

  refreshOperations(): void {
    this.loadOperations();
  }

  navigateToAccount(accountId: string): void {
    this.router.navigate(['/accounts', accountId]);
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

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  // Quick action methods
  navigateToCredit(): void {
    this.router.navigate(['/operations/credit']);
  }

  navigateToDebit(): void {
    this.router.navigate(['/operations/debit']);
  }

  navigateToTransfer(): void {
    this.router.navigate(['/operations/transfer']);
  }
}
