import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

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
import { Customer, CreateAccountRequest, BankAccount, CurrentAccount, SavingAccount } from '../../../models/bank.models';

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
  accountId: string | null = null;
  currentAccount: BankAccount | null = null;
  
  // Account types for dropdown
  accountTypes = [
    { value: 'SAVING', label: 'Saving Account' },
    { value: 'CURRENT', label: 'Current Account' }
  ];

  // Account status options
  statusOptions = [
    { value: 'ACTIVE', label: 'Active' },
    { value: 'SUSPENDED', label: 'Suspended' }
  ];

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private customerService: CustomerService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.accountForm = this.fb.group({
      customerId: ['', Validators.required],
      initialBalance: [0, [Validators.required, Validators.min(0)]],
      accountType: ['SAVING', Validators.required],
      overdraft: [0], // For current accounts
      interestRate: [0], // For saving accounts
      status: ['ACTIVE'] // For edit mode
    });
  }
  ngOnInit(): void {
    // Check if we're in edit mode
    this.accountId = this.route.snapshot.params['id'];
    this.isEditMode = !!this.accountId;
    this.isEdit = this.isEditMode;
    
    this.loadCustomers();
    
    if (this.isEditMode) {
      this.loadAccountForEdit();
    }
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

  loadAccountForEdit(): void {
    if (!this.accountId) return;
    
    this.loading = true;
    this.accountService.getAccount(this.accountId).subscribe({
      next: (account) => {
        this.currentAccount = account;
        this.populateFormForEdit(account);
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading account for edit:', error);
        alert('Failed to load account details');
        this.loading = false;
        this.router.navigate(['/accounts']);
      }
    });
  }
  populateFormForEdit(account: BankAccount): void {
    const formValue: any = {
      customerId: account.customer?.id,
      initialBalance: account.balance,
      accountType: account.type,
      status: account.status || 'ACTIVE'
    };

    if (account.type === 'CURRENT') {
      formValue.overdraft = (account as CurrentAccount).overdraft || 0;
    } else if (account.type === 'SAVING') {
      formValue.interestRate = (account as SavingAccount).interestRate || 0;
    }

    this.accountForm.patchValue(formValue);
    
    // Disable fields that shouldn't be editable in edit mode
    this.accountForm.get('customerId')?.disable();
    this.accountForm.get('accountType')?.disable();
    
    this.onAccountTypeChange(); // Update validators
  }  onSubmit(): void {
    if (this.accountForm.valid || (this.isEditMode && this.isFormValidForEdit())) {
      this.loading = true;
      const formValue = this.accountForm.getRawValue(); // Use getRawValue() to include disabled controls

      if (this.isEditMode) {
        this.updateAccount(formValue);
      } else {
        this.createAccount(formValue);
      }
    } else {
      // Mark all controls as touched to show validation errors
      Object.keys(this.accountForm.controls).forEach(key => {
        const control = this.accountForm.get(key);
        if (control && control.enabled) {
          control.markAsTouched();
        }
      });
    }
  }

  private isFormValidForEdit(): boolean {
    // For edit mode, only check enabled controls
    const controls = this.accountForm.controls;
    for (const key in controls) {
      const control = controls[key];
      if (control.enabled && control.invalid) {
        return false;
      }
    }
    return true;
  }

  createAccount(formValue: any): void {
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
  }  updateAccount(formValue: any): void {
    if (!this.accountId) return;

    const updateData: any = {
      balance: formValue.initialBalance,
      status: formValue.status
    };

    // Only include the relevant field for the current account type
    // Account type cannot be changed in edit mode, so we only update the appropriate field
    if (this.currentAccount?.type === 'CURRENT') {
      updateData.overdraft = formValue.overdraft;
    } else if (this.currentAccount?.type === 'SAVING') {
      updateData.interestRate = formValue.interestRate;
    }    this.accountService.updateAccount(this.accountId, updateData).subscribe({
      next: (updatedAccount) => {
        this.loading = false;
        alert('Account updated successfully!');
        
        // Update the current account and reload the form with fresh data
        this.currentAccount = updatedAccount;
        this.populateFormForEdit(updatedAccount);
        
        // Optionally redirect to accounts list after a short delay to let user see the changes
        // setTimeout(() => {
        //   this.router.navigate(['/accounts']);
        // }, 2000);
      },
      error: (error) => {
        this.loading = false;
        console.error('Error updating account:', error);
        alert('Failed to update account');
      }
    });
  }
  onCancel(): void {
    this.router.navigate(['/accounts']);
  }

  goToAccountsList(): void {
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
