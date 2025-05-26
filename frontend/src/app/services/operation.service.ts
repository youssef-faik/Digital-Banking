import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AccountOperation, AccountHistoryDTO, Page, AccountOperationDTO } from '../models/bank.models';
import { DebitRequest, CreditRequest, TransferRequest } from '../models/operation.models';

@Injectable({
  providedIn: 'root'
})
export class OperationService {
  private readonly baseUrl = 'http://localhost:8080/api/accounts';

  constructor(private http: HttpClient) { }

  /**
   * Perform debit operation
   */
  debit(request: DebitRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/debit`, request);
  }

  /**
   * Perform credit operation
   */
  credit(request: CreditRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/credit`, request);
  }

  /**
   * Perform transfer operation
   */
  transfer(request: TransferRequest): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/transfer`, request);
  }

  /**
   * Get operation by ID
   */
  getOperationById(operationId: number): Observable<AccountOperation> {
    return this.http.get<AccountOperation>(`${this.baseUrl}/${operationId}`);
  }
  /**
   * Get all operations with optional pagination
   */
  getAllOperations(page?: number, size?: number): Observable<Page<AccountOperationDTO>> {
    let params = new HttpParams();
    if (page !== undefined) params = params.set('page', page.toString());
    if (size !== undefined) params = params.set('size', size.toString());
    
    return this.http.get<Page<AccountOperationDTO>>(`${this.baseUrl}/operations`, { params });
  }/**
   * Get operations by account ID with pagination
   */
  getOperationsByAccountId(accountId: string, page: number = 0, size: number = 10): Observable<AccountHistoryDTO> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<AccountHistoryDTO>(`http://localhost:8080/api/accounts/${accountId}/operations`, { params });
  }

  /**
   * Get account history (alias for getOperationsByAccountId)
   */
  getAccountHistory(accountId: string, page: number = 0, size: number = 10): Observable<AccountHistoryDTO> {
    return this.getOperationsByAccountId(accountId, page, size);
  }

  /**
   * Search operations by criteria
   */
  searchOperations(
    accountId?: string,
    operationType?: 'DEBIT' | 'CREDIT',
    startDate?: string,
    endDate?: string,
    minAmount?: number,
    maxAmount?: number,
    page: number = 0,
    size: number = 10
  ): Observable<AccountOperation[]> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (accountId) params = params.set('accountId', accountId);
    if (operationType) params = params.set('operationType', operationType);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    if (minAmount !== undefined) params = params.set('minAmount', minAmount.toString());
    if (maxAmount !== undefined) params = params.set('maxAmount', maxAmount.toString());
    
    return this.http.get<AccountOperation[]>(`${this.baseUrl}/search`, { params });
  }

  /**
   * Get operations by date range
   */
  getOperationsByDateRange(
    accountId: string,
    startDate: string,
    endDate: string,
    page: number = 0,
    size: number = 10
  ): Observable<AccountOperation[]> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page.toString())
      .set('size', size.toString());
    
    return this.http.get<AccountOperation[]>(`${this.baseUrl}/account/${accountId}/dateRange`, { params });
  }

  /**
   * Get operation statistics for account
   */
  getOperationStats(accountId: string, period: 'WEEK' | 'MONTH' | 'YEAR' = 'MONTH'): Observable<any> {
    let params = new HttpParams().set('period', period);
    return this.http.get<any>(`${this.baseUrl}/account/${accountId}/stats`, { params });
  }

  /**
   * Cancel/Reverse operation (if allowed)
   */
  cancelOperation(operationId: number, reason: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${operationId}/cancel`, { reason });
  }

  /**
   * Get recent operations for dashboard
   */
  getRecentOperations(limit: number = 5): Observable<AccountOperation[]> {
    let params = new HttpParams().set('limit', limit.toString());
    return this.http.get<AccountOperation[]>(`${this.baseUrl}/recent`, { params });
  }
}
