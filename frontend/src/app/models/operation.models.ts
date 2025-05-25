export interface DebitRequest {
  accountId: string;
  amount: number;
  description: string;
}

export interface CreditRequest {
  accountId: string;
  amount: number;
  description: string;
}

export interface TransferRequest {
  accountSourceId: string;
  accountDestinationId: string;
  amount: number;
  description: string;
}

export interface CreateCurrentAccountRequest {
  initialBalance: number;
  overDraft: number;
  customerId: number;
}

export interface CreateSavingAccountRequest {
  initialBalance: number;
  interestRate: number;
  customerId: number;
}
