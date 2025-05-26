import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';

// Angular Material imports
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';

import { CustomerService } from '../../../services/customer.service';
import { AccountService } from '../../../services/account.service';
import { Customer, BankAccount } from '../../../models/bank.models';

@Component({
  selector: 'app-customer-details',
  standalone: true,  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTableModule,
    MatTooltipModule,
    RouterModule
  ],
  templateUrl: './customer-details.component.html',
  styleUrl: './customer-details.component.scss'
})
export class CustomerDetailsComponent implements OnInit {
  customer: Customer | null = null;
  customerAccounts: BankAccount[] = [];
  loading = false;
  customerId: number | null = null;
  
  displayedColumns: string[] = ['id', 'type', 'balance', 'status', 'actions'];

  constructor(
    private customerService: CustomerService,
    private accountService: AccountService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.customerId = Number(this.route.snapshot.params['id']);
    if (this.customerId) {
      this.loadCustomerDetails();
      this.loadCustomerAccounts();
    }
  }

  loadCustomerDetails(): void {
    if (!this.customerId) return;
    
    this.loading = true;
    this.customerService.getCustomer(this.customerId).subscribe({
      next: (customer) => {
        this.customer = customer;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading customer:', error);
        this.loading = false;
        this.router.navigate(['/customers']);
      }
    });
  }

  loadCustomerAccounts(): void {
    if (!this.customerId) return;
      this.accountService.getAllAccounts().subscribe({
      next: (response) => {
        // Handle both paginated and non-paginated responses
        const accounts = Array.isArray(response) ? response : response.content || [];
        // Filter accounts for this customer
        this.customerAccounts = accounts.filter(account => 
          account.customer?.id === this.customerId
        );
      },
      error: (error) => {
        console.error('Error loading customer accounts:', error);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/customers']);
  }

  editCustomer(): void {
    if (this.customerId) {
      this.router.navigate(['/customers/edit', this.customerId]);
    }
  }

  viewAccount(account: BankAccount): void {
    this.router.navigate(['/accounts', account.id]);
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString();
  }

  getAccountTypeColor(type: string): string {
    return type === 'CURRENT' ? 'primary' : 'accent';
  }

  getStatusColor(status: string): string {
    return status === 'ACTIVE' ? 'primary' : 'warn';
  }
}
