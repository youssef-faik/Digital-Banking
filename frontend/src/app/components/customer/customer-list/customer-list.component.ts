import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterModule } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { CustomerService } from '../../../services/customer.service';
import { Customer } from '../../../models/bank.models';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatPaginatorModule,
    MatSortModule,
    MatSnackBarModule,
    MatDialogModule,
    MatTooltipModule,
    RouterModule,
    ReactiveFormsModule
  ],
  templateUrl: './customer-list.component.html',
  styleUrl: './customer-list.component.scss'
})
export class CustomerListComponent implements OnInit {
  private customerService = inject(CustomerService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private router = inject(Router);

  customers: Customer[] = [];
  displayedColumns: string[] = ['id', 'name', 'email', 'actions'];
  isLoading = false;
  searchControl = new FormControl('');
  filteredCustomers: Customer[] = [];
  
  // Pagination properties
  pageSize = 10;
  pageIndex = 0;
  totalElements = 0;
  pageSizeOptions = [5, 10, 25, 50];
  ngOnInit() {
    this.loadCustomers();
    this.setupSearch();
  }

  setupSearch() {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(searchTerm => {
        this.pageIndex = 0; // Reset to first page when searching
        this.loadCustomers(searchTerm || '');
      });
  }

  loadCustomers(searchTerm: string = '') {
    this.isLoading = true;
    
    const loadMethod = searchTerm ? 
      this.customerService.searchCustomers(searchTerm, this.pageIndex, this.pageSize) :
      this.customerService.getCustomers(this.pageIndex, this.pageSize);
    
    loadMethod.subscribe({
      next: (response: any) => {
        this.customers = response.content || [];
        this.filteredCustomers = this.customers;
        this.totalElements = response.totalElements || 0;
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        this.snackBar.open(
          'Failed to load customers. Please try again.',
          'Close',
          { duration: 5000, panelClass: 'error-snackbar' }
        );
      }
    });
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCustomers(this.searchControl.value || '');
  }

  filterCustomers(searchTerm: string) {
    // This method is no longer used for filtering since we use server-side pagination
    // But keeping it for backward compatibility
    this.loadCustomers(searchTerm);
  }

  addCustomer() {
    this.router.navigate(['/customers/new']);
  }

  editCustomer(customer: Customer) {
    this.router.navigate(['/customers/edit', customer.id]);
  }

  viewCustomer(customer: Customer) {
    this.router.navigate(['/customers', customer.id]);
  }

  deleteCustomer(customer: Customer) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px', // Standard width for simple confirmation
      data: {
        title: 'Delete Customer',
        message: `Are you sure you want to delete customer "${customer.name}"?`, // Simplified message
        confirmText: 'Delete',
        cancelText: 'Cancel',
        confirmButtonColor: 'warn' // Standard color for delete confirmation
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && customer.id) {
        this.customerService.deleteCustomer(customer.id).subscribe({
          next: () => {
            this.snackBar.open('Customer deleted successfully!', 'Close', {
              duration: 3000,
              panelClass: 'success-snackbar'
            });
            this.loadCustomers(this.searchControl.value || ''); // Reload current page with search term
          },
          error: (error: any) => {
            console.error('Error deleting customer:', error);
            let errorMessage = 'Failed to delete customer. Please try again.';
            let errorTitle = 'Deletion Failed';

            if (error.status === 409) { // Customer has associated bank accounts
              errorTitle = 'Unable to Delete Customer';
              // Prioritize backend message, but provide a more specific fallback
              errorMessage = error.error?.message || `Cannot delete customer "${customer.name}" (ID: ${customer.id}) as they may have associated bank accounts or other dependencies. Please check the customer\\'s details or contact support if the issue persists.`;
              
              // Open a new dialog for this specific alert
              this.dialog.open(ConfirmDialogComponent, {
                width: '450px',
                data: {
                  title: errorTitle,
                  message: errorMessage,
                  confirmText: 'OK',
                  cancelText: null, // No cancel button for this alert dialog
                  confirmButtonColor: 'primary' // Neutral color for OK
                }
              });
            } else {
              // For other errors, use the snackbar
              if (error.status === 404) {
                errorMessage = `Customer "${customer.name}" (ID: ${customer.id}) was not found. It may have already been deleted.`;
              } else if (error.status === 403) {
                errorMessage = `You do not have the necessary permissions to delete customer "${customer.name}" (ID: ${customer.id}). Please contact an administrator if you believe this is an error.`;
              } else if (error.error?.message) {
                errorMessage = error.error.message;
              } else {
                // Generic fallback for other unexpected errors
                errorMessage = `An unexpected error occurred while trying to delete customer "${customer.name}" (ID: ${customer.id}). Please try again or contact support.`;
              }
              
              this.snackBar.open(errorMessage, 'Close', {
                duration: 7000,
                panelClass: 'error-snackbar'
              });
            }
          }
        });
      }
    });
  }
}

// Confirm Dialog Component (inline for simplicity)
import { Component as DialogComponent, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@DialogComponent({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>{{data.title}}</h2>
    <div mat-dialog-content>
      <p style="white-space: pre-line; line-height: 1.6;">{{data.message}}</p>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()" *ngIf="data.cancelText">{{data.cancelText}}</button>
      <button mat-raised-button [color]="data.confirmButtonColor || 'warn'" (click)="onConfirm()">{{data.confirmText}}</button>
    </div>
  `
})
export class ConfirmDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
