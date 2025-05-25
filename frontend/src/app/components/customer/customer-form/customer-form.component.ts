import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, ActivatedRoute } from '@angular/router';

import { CustomerService } from '../../../services/customer.service';
import { Customer } from '../../../models/bank.models';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  templateUrl: './customer-form.component.html',
  styleUrl: './customer-form.component.scss'
})
export class CustomerFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private customerService = inject(CustomerService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  customerForm: FormGroup;
  isLoading = false;
  isEditMode = false;
  customerId: number | null = null;

  constructor() {
    this.customerForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]]
    });
  }

  ngOnInit() {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.customerId = +params['id'];
        this.loadCustomer(this.customerId);
      }
    });
  }

  loadCustomer(id: number) {
    this.isLoading = true;
    this.customerService.getCustomer(id).subscribe({
      next: (customer: Customer) => {
        this.customerForm.patchValue({
          name: customer.name,
          email: customer.email
        });
        this.isLoading = false;
      },
      error: (error: any) => {
        this.isLoading = false;
        this.snackBar.open(
          'Failed to load customer details. Please try again.',
          'Close',
          { duration: 5000, panelClass: 'error-snackbar' }
        );
        this.router.navigate(['/customers']);
      }
    });
  }

  onSubmit() {
    if (this.customerForm.valid) {
      this.isLoading = true;
      const customerData: Customer = this.customerForm.value;

      const operation = this.isEditMode 
        ? this.customerService.updateCustomer(this.customerId!, customerData)
        : this.customerService.createCustomer(customerData);

      operation.subscribe({
        next: (customer: Customer) => {
          this.snackBar.open(
            `Customer ${this.isEditMode ? 'updated' : 'created'} successfully!`,
            'Close',
            { duration: 3000, panelClass: 'success-snackbar' }
          );
          this.router.navigate(['/customers']);
        },
        error: (error: any) => {
          this.isLoading = false;
          this.snackBar.open(
            `Failed to ${this.isEditMode ? 'update' : 'create'} customer. Please try again.`,
            'Close',
            { duration: 5000, panelClass: 'error-snackbar' }
          );
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }

  cancel() {
    this.router.navigate(['/customers']);
  }

  getFormTitle(): string {
    return this.isEditMode ? 'Edit Customer' : 'Add New Customer';
  }

  getSubmitButtonText(): string {
    if (this.isLoading) {
      return this.isEditMode ? 'Updating...' : 'Creating...';
    }
    return this.isEditMode ? 'Update Customer' : 'Create Customer';
  }
}
