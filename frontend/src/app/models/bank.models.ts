export interface Customer {
  id?: number;
  name: string;
  email: string;
}

export interface BankAccount {
  id: string;
  balance: number;
  createdAt: string;
  status: AccountStatus;
  type: 'CURRENT' | 'SAVING' | null; // Allow null if backend can send it
  customer: Customer | null; // Expect a mapped Customer object, can be null
  overdraft?: number;
  interestRate?: number;
}

export interface CurrentAccount extends BankAccount {
  type: 'CURRENT';
  overdraft: number;
}

export interface SavingAccount extends BankAccount {
  type: 'SAVING';
  interestRate: number;
}

// DTO for Customer (matching backend)
export interface CustomerDTO {
  id: number;
  name: string;
  email: string;
}

// DTO for BankAccount (matching backend)
export interface BankAccountDTO {
  id: string;
  balance: number;
  createdAt: string;
  status: AccountStatus | null;
  type: 'CURRENT' | 'SAVING' | null;
  customer: CustomerDTO; // Matches backend field name
  overdraft?: number;
  interestRate?: number;
  // Backend might have other fields like 'className' if it's sending polymorphic types directly
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

export enum AccountStatus {
  ACTIVE = 'ACTIVE',
  SUSPENDED = 'SUSPENDED'
}

// Generic Page interface for paginated responses
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
  empty: boolean;
}

// DTO for AccountOperation (matching backend)
export interface AccountOperationDTO {
  id: number;
  operationDate: string;
  amount: number;
  type: 'DEBIT' | 'CREDIT';
  description: string;
  bankAccountId: string;
  performedByUsername: string;
}
