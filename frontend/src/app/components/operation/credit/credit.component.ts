import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

import { OperationService } from '../../../services/operation.service';
import { CreditRequest } from '../../../models/operation.models';

@Component({
  selector: 'app-credit',
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
  templateUrl: './credit.component.html',
  styleUrl: './credit.component.scss'
})
export class CreditComponent {
  private fb = inject(FormBuilder);
  private operationService = inject(OperationService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  creditForm: FormGroup;
  isLoading = false;

  constructor() {
    this.creditForm = this.fb.group({
      accountId: ['', [Validators.required]],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', [Validators.required]]
    });
  }

  onSubmit() {
    if (this.creditForm.valid) {
      this.isLoading = true;
      const formValue = this.creditForm.value;
      
      const creditRequest: CreditRequest = {
        accountId: formValue.accountId,
        amount: formValue.amount,
        description: formValue.description
      };

      this.operationService.credit(creditRequest).subscribe({
        next: (operation) => {
          this.snackBar.open(
            `Credit operation successful! Amount: $${formValue.amount}`,
            'Close',
            { duration: 3000, panelClass: 'success-snackbar' }
          );
          this.creditForm.reset();
          this.router.navigate(['/accounts', formValue.accountId]);
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open(
            error.error?.message || 'Credit operation failed. Please try again.',
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
    this.router.navigate(['/dashboard']);
  }
}
