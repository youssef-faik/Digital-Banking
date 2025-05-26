import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  // Default route
  { 
    path: '', 
    redirectTo: '/login', 
    pathMatch: 'full' 
  },
  
  // Auth routes (no guard needed)
  {
    path: 'login',
    loadComponent: () => import('./components/auth/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'register',
    loadComponent: () => import('./components/auth/register/register.component')
      .then(m => m.RegisterComponent)
  },
  
  // Protected routes (require authentication)
  {
    path: 'dashboard',
    loadComponent: () => import('./components/dashboard/dashboard.component')
      .then(m => m.DashboardComponent),
    canActivate: [authGuard]
  },
    // Customer routes
  {
    path: 'customers',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/customer/customer-list/customer-list.component')
          .then(m => m.CustomerListComponent)
      },
      {
        path: 'new',
        loadComponent: () => import('./components/customer/customer-form/customer-form.component')
          .then(m => m.CustomerFormComponent)
      },
      {
        path: 'edit/:id',
        loadComponent: () => import('./components/customer/customer-form/customer-form.component')
          .then(m => m.CustomerFormComponent)
      },
      {
        path: ':id',
        loadComponent: () => import('./components/customer/customer-details/customer-details.component')
          .then(m => m.CustomerDetailsComponent)
      }
    ]
  },
  
  // Account routes
  {
    path: 'accounts',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/account/account-list/account-list.component')
          .then(m => m.AccountListComponent)
      },
      {
        path: 'new',
        loadComponent: () => import('./components/account/account-form/account-form.component')
          .then(m => m.AccountFormComponent)
      },
      {
        path: 'edit/:id',
        loadComponent: () => import('./components/account/account-form/account-form.component')
          .then(m => m.AccountFormComponent)
      },
      {
        path: ':id',
        loadComponent: () => import('./components/account/account-details/account-details.component')
          .then(m => m.AccountDetailsComponent)
      }
    ]
  },
    // Operation routes
  {
    path: 'operations',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/operation/operation-list/operation-list.component')
          .then(m => m.OperationListComponent)
      },
      {
        path: 'credit',
        loadComponent: () => import('./components/operation/credit/credit.component')
          .then(m => m.CreditComponent)
      },
      {
        path: 'debit',
        loadComponent: () => import('./components/operation/debit/debit.component')
          .then(m => m.DebitComponent)
      },
      {
        path: 'transfer',
        loadComponent: () => import('./components/operation/transfer/transfer.component')
          .then(m => m.TransferComponent)
      }
    ]
  },
    // Profile routes
  {
    path: 'profile',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./components/profile/profile.component')
          .then(m => m.ProfileComponent)
      },
      {
        path: 'change-password',
        loadComponent: () => import('./components/auth/change-password/change-password.component')
          .then(m => m.ChangePasswordComponent)
      }
    ]
  },
  
  // Catch all route - redirect to login
  { 
    path: '**', 
    redirectTo: '/login' 
  }
];
