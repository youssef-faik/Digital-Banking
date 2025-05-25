export interface Customer {
  id?: number;
  name: string;
  email: string;
}

export interface BankAccount {
  id: string;
  balance: number;
  createdAt: string;
  customer: Customer;
  type: 'CURRENT' | 'SAVING';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  accountType?: 'CURRENT_ACCOUNT' | 'SAVING_ACCOUNT'; // For backward compatibility
}

export interface CurrentAccount extends BankAccount {
  overdraft: number;
}

export interface SavingAccount extends BankAccount {
  interestRate: number;
}

export interface AccountOperation {
  id?: number;
  operationDate: string;
  amount: number;
  type: 'DEBIT' | 'CREDIT';
  description: string;
  bankAccount?: BankAccount;
}

export interface AccountHistory {
  accountId: string;
  balance: number;
  currentPage: number;
  totalPages: number;
  pageSize: number;
  accountOperationDTOS: AccountOperation[];
}

export interface AccountHistoryDTO {
  accountId: string;
  balance: number;
  currentPage: number;
  totalPages: number;
  pageSize: number;
  accountOperationDTOS: AccountOperation[];
}

export interface CreateAccountRequest {
  customerId: number;
  initialBalance: number;
  overdraft?: number; // for current accounts
  interestRate?: number; // for savings accounts
}

export interface DashboardStats {
  totalCustomers: number;
  totalAccounts: number;
  totalOperations: number;
  totalBalance: number;
}

export interface DataPoint {
  label: string;
  value: number;
}

export interface DashboardChartData {
  operationsTrend: DataPoint[];
  accountTypeDistribution: DataPoint[];
}
