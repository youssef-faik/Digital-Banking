import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

// Angular Material imports
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AccountService } from '../../../services/account.service';
import { CustomerService } from '../../../services/customer.service';
import { Customer, CreateAccountRequest } from '../../../models/bank.models';

@Component({
  selector: 'app-account-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './account-form.component.html',
  styleUrl: './account-form.component.scss'
})
export class AccountFormComponent implements OnInit {
  accountForm: FormGroup;
  customers: Customer[] = [];
  loading = false;
  isEditMode = false;
  isEdit = false; // Alias for template compatibility
  
  // Account types for dropdown
  accountTypes = [
    { value: 'SAVING', label: 'Saving Account' },
    { value: 'CURRENT', label: 'Current Account' }
  ];

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private customerService: CustomerService,
    private router: Router
  ) {
    this.accountForm = this.fb.group({
      customerId: ['', Validators.required],
      initialBalance: [0, [Validators.required, Validators.min(0)]],
      accountType: ['SAVING', Validators.required],
      overdraft: [0], // For current accounts
      interestRate: [0] // For saving accounts
    });
  }

  ngOnInit(): void {
    this.loadCustomers();
  }

  loadCustomers(): void {
    this.customerService.getAllCustomers().subscribe({
      next: (data) => {
        this.customers = data.content || data || [];
      },
      error: (error) => {
        console.error('Error loading customers:', error);
        alert('Failed to load customers');
      }
    });
  }

  onSubmit(): void {
    if (this.accountForm.valid) {
      this.loading = true;
      const formValue = this.accountForm.value;
      
      const request: CreateAccountRequest = {
        customerId: formValue.customerId,
        initialBalance: formValue.initialBalance,
        overdraft: formValue.accountType === 'CURRENT' ? formValue.overdraft : undefined,
        interestRate: formValue.accountType === 'SAVING' ? formValue.interestRate : undefined
      };

      const accountCreation$ = formValue.accountType === 'CURRENT' 
        ? this.accountService.createCurrentAccount(request)
        : this.accountService.createSavingAccount(request);

      accountCreation$.subscribe({
        next: (account) => {
          this.loading = false;
          alert('Account created successfully!');
          this.router.navigate(['/accounts']);
        },
        error: (error) => {
          this.loading = false;
          console.error('Error creating account:', error);
          alert('Failed to create account');
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/accounts']);
  }

  onAccountTypeChange(): void {
    const accountType = this.accountForm.get('accountType')?.value;
    
    if (accountType === 'CURRENT') {
      this.accountForm.get('overdraft')?.setValidators([Validators.required, Validators.min(0)]);
      this.accountForm.get('interestRate')?.clearValidators();
    } else {
      this.accountForm.get('interestRate')?.setValidators([Validators.required, Validators.min(0)]);
      this.accountForm.get('overdraft')?.clearValidators();
    }
    
    this.accountForm.get('overdraft')?.updateValueAndValidity();
    this.accountForm.get('interestRate')?.updateValueAndValidity();
  }
}
