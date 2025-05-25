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
        this.filterCustomers(searchTerm || '');
      });
  }

  loadCustomers() {
    this.isLoading = true;
    this.customerService.getCustomers().subscribe({
      next: (response: any) => {
        this.customers = response.content || response;
        this.filteredCustomers = this.customers;
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

  filterCustomers(searchTerm: string) {
    if (!searchTerm) {
      this.filteredCustomers = this.customers;
      return;
    }

    const term = searchTerm.toLowerCase();
    this.filteredCustomers = this.customers.filter(customer =>
      customer.name.toLowerCase().includes(term) ||
      customer.email.toLowerCase().includes(term)
    );
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
      width: '400px',
      data: {
        title: 'Delete Customer',
        message: `Are you sure you want to delete customer ${customer.name}?`,
        confirmText: 'Delete',
        cancelText: 'Cancel'
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
            this.loadCustomers();
          },
          error: (error: any) => {
            this.snackBar.open(
              'Failed to delete customer. Please try again.',
              'Close',
              { duration: 5000, panelClass: 'error-snackbar' }
            );
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
      <p>{{data.message}}</p>
    </div>
    <div mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">{{data.cancelText}}</button>
      <button mat-raised-button color="warn" (click)="onConfirm()">{{data.confirmText}}</button>
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
