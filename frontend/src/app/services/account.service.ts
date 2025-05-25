import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BankAccount, AccountHistoryDTO, CreateAccountRequest } from '../models/bank.models';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private readonly baseUrl = 'http://localhost:8080/api/accounts';

  constructor(private http: HttpClient) { }

  /**
   * Get all accounts with optional pagination
   */
  getAllAccounts(page?: number, size?: number): Observable<BankAccount[]> {
    let params = new HttpParams();
    if (page !== undefined) params = params.set('page', page.toString());
    if (size !== undefined) params = params.set('size', size.toString());
    
    return this.http.get<BankAccount[]>(this.baseUrl, { params });
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
    return this.http.post<BankAccount>(`${this.baseUrl}/savings`, request);
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
   * Update account balance (for admin purposes)
   */
  updateAccountBalance(accountId: string, newBalance: number): Observable<BankAccount> {
    return this.http.put<BankAccount>(`${this.baseUrl}/${accountId}/balance`, { balance: newBalance });
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
    return this.http.get<BankAccount[]>(`${this.baseUrl}/customer/${customerId}`);
  }

  /**
   * Search accounts by criteria
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
    return this.http.patch<BankAccount>(`${this.baseUrl}/${accountId}/status`, { status });
  }
}
