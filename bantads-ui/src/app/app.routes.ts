import { Routes } from '@angular/router';

import { DashboardAdminComponent } from './features/administrador/screens/dashbord-admin/dashbord-admin';
import { CrudGerentesComponent } from './features/administrador/screens/crud-gerentes/crud-gerentes';
import { RelatorioComponent } from './features/administrador/screens/relatorio/relatorio';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./features/public/home/home').then(m => m.Home)
    },
    {
        path: 'dashboard-gerente',
        loadComponent: () => import('./features/gerente/screens/dashboard-gerente/dashboard-gerente').then(m => m.DashboardGerente)
    },
    {
        path: 'cliente/autocadastro',
        loadComponent: () => import('./features/cliente/autocadastro/presentation/screens/autocadastro/autocadastro').then(m => m.AutocadastroComponent)
    },
    {
      path: 'cliente/dashboard',
      loadComponent: () => import('./features/cliente/screens/dashboard/dashboard').then(m => m.Dashboard)
    },
    {
        path: 'admin/dashboard',
        loadComponent: () => import('./features/administrador/screens/dashbord-admin/dashbord-admin').then(m => m.DashboardAdminComponent)
    },
    {
        path: 'admin/gerentes',
        loadComponent: () => import('./features/administrador/screens/crud-gerentes/crud-gerentes').then(m => m.CrudGerentesComponent)
    },
    {
        path: 'admin/relatorios',
        loadComponent: () => import('./features/administrador/screens/relatorio/relatorio').then(m => m.RelatorioComponent)
    },
    {
      path: 'auth/login',
      loadComponent: () => import('./features/auth/presentation/screens/login/login').then(m => m.Login)
    }



];
