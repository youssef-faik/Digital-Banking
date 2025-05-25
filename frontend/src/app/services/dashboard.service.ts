import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, forkJoin } from 'rxjs';
import { map } from 'rxjs/operators';

export interface DataPointDTO {
  label: string;
  value: number;
}

export interface DashboardChartDataDTO {
  operationsTrend: DataPointDTO[];
  accountTypeDistribution: DataPointDTO[];
  monthlyOperationsVolume: DataPointDTO[];
  balanceDistribution: DataPointDTO[];
}

export interface DashboardOverview {
  stats: DashboardStatsDTO;
  chartData: DashboardChartDataDTO;
}

export interface RecentTransactionDTO {
  id: number;
  type: 'CREDIT' | 'DEBIT' | 'TRANSFER';
  amount: number;
  description: string;
  operationDate: string;
  accountId: string;
  customerName: string;
}

export interface FinancialMetricsDTO {
  totalRevenue: number;
  totalExpenses: number;
  netProfit: number;
  growthRate: number;
  transactionFees: number;
  averageTransactionSize: number;
}

export interface DashboardStatsDTO {
  totalCustomers: number;
  totalAccounts: number;
  totalBalance: number;
  totalOperationsToday: number;
  averageAccountBalance: number;
  activeAccountsCount: number;
  suspendedAccountsCount: number;
  recentOperationsCount: number;
  // Additional comprehensive statistics
  totalOperationsThisWeek: number;
  totalOperationsThisMonth: number;
  totalCreditAmount: number;
  totalDebitAmount: number;
  currentAccountsCount: number;
  savingAccountsCount: number;
  highestAccountBalance: number;
  lowestAccountBalance: number;
  operationsLast24Hours: number;
  averageDailyTransactionVolume: number;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private readonly baseUrl = 'http://localhost:8080/api/dashboard';

  constructor(private http: HttpClient) { }

  /**
   * Get dashboard statistics
   */
  getDashboardStats(): Observable<DashboardStatsDTO> {
    return this.http.get<DashboardStatsDTO>(`${this.baseUrl}/stats`);
  }

  /**
   * Get dashboard chart data
   */
  getDashboardChartData(): Observable<DashboardChartDataDTO> {
    return this.http.get<DashboardChartDataDTO>(`${this.baseUrl}/chart-data`);
  }

  /**
   * Get complete dashboard overview (stats + chart data)
   */
  getDashboardOverview(): Observable<DashboardOverview> {
    return forkJoin({
      stats: this.getDashboardStats(),
      chartData: this.getDashboardChartData()
    });
  }

  /**
   * Get operations trend for specific period
   */
  getOperationsTrend(period: 'WEEK' | 'MONTH' | 'YEAR' = 'WEEK'): Observable<DataPointDTO[]> {
    let params = new HttpParams().set('period', period);
    return this.http.get<DataPointDTO[]>(`${this.baseUrl}/operations-trend`, { params });
  }

  /**
   * Get account type distribution
   */
  getAccountTypeDistribution(): Observable<DataPointDTO[]> {
    return this.http.get<DataPointDTO[]>(`${this.baseUrl}/account-types`);
  }

  /**
   * Get balance distribution by account ranges
   */
  getBalanceDistribution(): Observable<DataPointDTO[]> {
    return this.http.get<DataPointDTO[]>(`${this.baseUrl}/balance-distribution`);
  }

  /**
   * Get monthly operations volume
   */
  getMonthlyOperationsVolume(months: number = 12): Observable<DataPointDTO[]> {
    let params = new HttpParams().set('months', months.toString());
    return this.http.get<DataPointDTO[]>(`${this.baseUrl}/monthly-volume`, { params });
  }

  /**
   * Get top customers by balance
   */
  getTopCustomersByBalance(limit: number = 10): Observable<any[]> {
    let params = new HttpParams().set('limit', limit.toString());
    return this.http.get<any[]>(`${this.baseUrl}/top-customers`, { params });
  }

  /**
   * Get operations summary by type
   */
  getOperationsSummary(startDate?: string, endDate?: string): Observable<any> {
    let params = new HttpParams();
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    
    return this.http.get<any>(`${this.baseUrl}/operations-summary`, { params });
  }

  /**
   * Get account growth over time
   */
  getAccountGrowth(period: 'WEEK' | 'MONTH' | 'YEAR' = 'MONTH'): Observable<DataPointDTO[]> {
    let params = new HttpParams().set('period', period);
    return this.http.get<DataPointDTO[]>(`${this.baseUrl}/account-growth`, { params });
  }

  /**
   * Get revenue trends
   */
  getRevenueTrends(period: 'WEEK' | 'MONTH' | 'YEAR' = 'MONTH'): Observable<DataPointDTO[]> {
    let params = new HttpParams().set('period', period);
    return this.http.get<DataPointDTO[]>(`${this.baseUrl}/revenue-trends`, { params });
  }

  /**
   * Get customer activity metrics
   */
  getCustomerActivityMetrics(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/customer-activity`);
  }

  /**
   * Get real-time dashboard metrics (for live updates)
   */
  getRealTimeMetrics(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/realtime`);
  }

  /**
   * Get recent transactions for dashboard
   */
  getRecentTransactions(limit: number = 10): Observable<RecentTransactionDTO[]> {
    let params = new HttpParams().set('limit', limit.toString());
    return this.http.get<RecentTransactionDTO[]>(`${this.baseUrl}/recent-transactions`, { params });
  }

  /**
   * Get financial metrics for dashboard
   */
  getFinancialMetrics(): Observable<FinancialMetricsDTO> {
    return this.http.get<FinancialMetricsDTO>(`${this.baseUrl}/financial-metrics`);
  }

  /**
   * Format chart data for ng2-charts
   */
  formatChartData(data: DataPointDTO[], chartType: 'line' | 'bar' | 'pie' | 'doughnut' = 'line'): any {
    const labels = data.map(item => item.label);
    const values = data.map(item => item.value);

    if (chartType === 'pie' || chartType === 'doughnut') {
      return {
        labels: labels,
        datasets: [{
          data: values,
          backgroundColor: this.generateColors(data.length),
          borderWidth: 1
        }]
      };
    }

    return {
      labels: labels,
      datasets: [{
        label: 'Data',
        data: values,
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        tension: 0.1
      }]
    };
  }

  /**
   * Generate colors for charts
   */
  private generateColors(count: number): string[] {
    const colors = [
      '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0',
      '#9966FF', '#FF9F40', '#FF6384', '#C9CBCF',
      '#4BC0C0', '#FF6384'
    ];
    
    return Array(count).fill(0).map((_, index) => 
      colors[index % colors.length]
    );
  }
}
