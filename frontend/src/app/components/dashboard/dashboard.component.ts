import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject, takeUntil } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatGridListModule } from '@angular/material/grid-list';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';
import { DashboardService, DashboardStatsDTO, DashboardChartDataDTO, RecentTransactionDTO, FinancialMetricsDTO } from '../../services/dashboard.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatGridListModule,
    BaseChartDirective
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {
  stats: DashboardStatsDTO | null = null;
  chartData: DashboardChartDataDTO | null = null;
  recentTransactions: RecentTransactionDTO[] = [];
  financialMetrics: FinancialMetricsDTO | null = null;
  isLoading = true;
  
  // Chart configurations
  operationsTrendChartData: ChartData<'line'> = {
    labels: [],
    datasets: []
  };
  
  accountTypeChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: []
  };
  
  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };
  
  doughnutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    plugins: {
      legend: {
        display: true,
        position: 'right'
      }
    }
  };
  
  private destroy$ = new Subject<void>();

  constructor(
    private dashboardService: DashboardService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }  loadDashboardData(): void {
    this.isLoading = true;
    console.log('📊 Loading dashboard data...');
    console.log('🔐 User authenticated:', this.authService.isAuthenticated());
    console.log('👤 Current user:', this.authService.getCurrentUser());
    
    // For now, use the existing getDashboardOverview method
    this.dashboardService.getDashboardOverview()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data: any) => {
          console.log('✅ Dashboard data loaded successfully:', data);
          this.stats = data.stats;
          this.chartData = data.chartData;
          this.setupCharts();
          this.isLoading = false;
        },
        error: (error: any) => {
          console.error('❌ Error loading dashboard data:', error);
          console.log('📊 Error status:', error.status);
          console.log('📊 Error message:', error.message);
          this.isLoading = false;
        }
      });
  }  private setupCharts(): void {
    if (!this.chartData) return;

    // Operations Trend Chart
    this.operationsTrendChartData = this.dashboardService.formatChartData(
      this.chartData.operationsTrend, 
      'line'
    );

    // Account Type Distribution Chart
    this.accountTypeChartData = this.dashboardService.formatChartData(
      this.chartData.accountTypeDistribution, 
      'doughnut'
    );
  }

  refreshData(): void {
    this.loadDashboardData();
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }
  formatNumber(num: number): string {
    return new Intl.NumberFormat('en-US').format(num);
  }

  logout(): void {
    this.authService.logout();
  }
}
