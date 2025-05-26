import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BankAccount, AccountHistoryDTO, CreateAccountRequest, BankAccountDTO, AccountStatus, CurrentAccount, SavingAccount } from '../models/bank.models';

interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
  };
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
  numberOfElements: number;
}

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private readonly baseUrl = 'http://localhost:8080/api/accounts';

  constructor(private http: HttpClient) { }  /**
   * Get all accounts with optional pagination
   */
  getAllAccounts(page?: number, size?: number): Observable<PageResponse<BankAccount> | BankAccount[]> {
    let params = new HttpParams();
    if (page !== undefined) params = params.set('page', page.toString());
    if (size !== undefined) params = params.set('size', size.toString());

    if (page !== undefined && size !== undefined) {
      // Return paginated response
      return this.http.get<PageResponse<BankAccountDTO>>(this.baseUrl, { params }).pipe(
        map(response => ({
          ...response,
          content: response.content.map(dto => ({
            ...dto,
            customer: dto.customer, // Backend now uses customer field
            type: dto.type, // Ensure type is correctly mapped
            status: dto.status || 'ACTIVE' // Provide a default status if null
          } as BankAccount))
        }))
      );
    } else {
      // Return simple array (backward compatibility)
      return this.http.get<PageResponse<BankAccountDTO>>(this.baseUrl, { params }).pipe(
        map(response => {
          return response.content.map(dto => ({
            ...dto,
            customer: dto.customer, // Backend now uses customer field
            type: dto.type, // Ensure type is correctly mapped
            status: dto.status || 'ACTIVE' // Provide a default status if null
          } as BankAccount));
        })
      );
    }
  }

  /**
   * Get account by ID
   */
  getAccountById(accountId: string): Observable<BankAccount> {
    return this.http.get<BankAccount>(`${this.baseUrl}/${accountId}`);
  }

  /**
   * Alias for getAccountById (for backward compatibility)
   */
  getAccount(accountId: string): Observable<BankAccount> {
    return this.getAccountById(accountId);
  }

  /**
   * Get account history with pagination
   */
  getAccountHistory(accountId: string, page: number = 0, size: number = 5): Observable<AccountHistoryDTO> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<AccountHistoryDTO>(`${this.baseUrl}/${accountId}/pageOperations`, { params });
  }
  /**
   * Create a new savings account
   */
  createSavingsAccount(request: CreateAccountRequest): Observable<BankAccount> {
    return this.http.post<BankAccount>(`${this.baseUrl}/saving`, request);
  }

  /**
   * Alias for createSavingsAccount (for backward compatibility)
   */
  createSavingAccount(request: CreateAccountRequest): Observable<BankAccount> {
    return this.createSavingsAccount(request);
  }

  /**
   * Create a new current account
   */
  createCurrentAccount(request: CreateAccountRequest): Observable<BankAccount> {
    return this.http.post<BankAccount>(`${this.baseUrl}/current`, request);
  }

  /**
   * Update account (using the new backend endpoint)
   */
  updateAccount(accountId: string, updateData: any): Observable<BankAccount> {
    return this.http.put<BankAccount>(`${this.baseUrl}/${accountId}`, updateData);
  }

  /**
   * Update account balance (for admin purposes) - deprecated, use updateAccount instead
   */
  updateAccountBalance(accountId: string, newBalance: number): Observable<BankAccount> {
    return this.updateAccount(accountId, { balance: newBalance });
  }

  /**
   * Delete account
   */
  deleteAccount(accountId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${accountId}`);
  }

  /**
   * Get accounts by customer ID
   */
  getAccountsByCustomerId(customerId: number): Observable<BankAccount[]> {
    return this.http.get<BankAccount[]>(`${this.baseUrl}/by-customer/${customerId}`);
  }

  /**
   * Search accounts by criteria (not implemented in backend yet)
   */
  searchAccounts(keyword: string, page: number = 0, size: number = 10): Observable<BankAccount[]> {
    let params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<BankAccount[]>(`${this.baseUrl}/search`, { params });
  }

  /**
   * Activate/Deactivate account
   */
  toggleAccountStatus(accountId: string, status: 'ACTIVE' | 'SUSPENDED'): Observable<BankAccount> {
    return this.http.patch<BankAccount>(`${this.baseUrl}/${accountId}/status`, status);
  }
}
