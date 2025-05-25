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
import { TransferRequest } from '../../../models/operation.models';

@Component({
  selector: 'app-transfer',
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
  templateUrl: './transfer.component.html',
  styleUrl: './transfer.component.scss'
})
export class TransferComponent {
  private fb = inject(FormBuilder);
  private operationService = inject(OperationService);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  transferForm: FormGroup;
  isLoading = false;

  constructor() {
    this.transferForm = this.fb.group({
      accountSourceId: ['', [Validators.required]],
      accountDestinationId: ['', [Validators.required]],
      amount: ['', [Validators.required, Validators.min(0.01)]],
      description: ['', [Validators.required]]
    }, { validators: this.differentAccountsValidator });
  }

  differentAccountsValidator(form: FormGroup) {
    const source = form.get('accountSourceId');
    const destination = form.get('accountDestinationId');
    
    if (source && destination && source.value && destination.value && source.value === destination.value) {
      destination.setErrors({ sameAccount: true });
      return { sameAccount: true };
    }
    
    if (destination?.hasError('sameAccount') && source?.value !== destination?.value) {
      const errors = { ...destination.errors };
      delete errors['sameAccount'];
      destination.setErrors(Object.keys(errors).length ? errors : null);
    }
    
    return null;
  }

  onSubmit() {
    if (this.transferForm.valid) {
      this.isLoading = true;
      const formValue = this.transferForm.value;
      
      const transferRequest: TransferRequest = {
        accountSourceId: formValue.accountSourceId,
        accountDestinationId: formValue.accountDestinationId,
        amount: formValue.amount,
        description: formValue.description
      };

      this.operationService.transfer(transferRequest).subscribe({
        next: () => {
          this.snackBar.open(
            `Transfer successful! Amount: $${formValue.amount} from ${formValue.accountSourceId} to ${formValue.accountDestinationId}`,
            'Close',
            { duration: 5000, panelClass: 'success-snackbar' }
          );
          this.transferForm.reset();
          this.router.navigate(['/accounts', formValue.accountSourceId]);
        },
        error: (error) => {
          this.isLoading = false;
          this.snackBar.open(
            error.error?.message || 'Transfer operation failed. Please try again.',
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
