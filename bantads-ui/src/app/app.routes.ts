import { Routes } from '@angular/router';

import { DashboardAdminComponent } from './features/administrador/screens/dashbord-admin/dashbord-admin';
import { CrudGerentesComponent } from './features/administrador/screens/crud-gerentes/crud-gerentes';
import { RelatorioComponent } from './features/administrador/screens/relatorio/relatorio';
import { Home } from './features/public/home/home';

export const routes: Routes = [
    {
        path: '',
        component: Home,
        title: "BANTADS - Banco do TADS"
    },
    {
      path: 'auth/login',
      loadComponent: () => import('./features/auth/presentation/screens/login/login').then(m => m.Login),
      title: "BANTADS - Realizar Login"
    },
    {
        path: 'cliente/autocadastro',
        loadComponent: () => import('./features/cliente/screens/autocadastro/autocadastro').then(m => m.AutocadastroComponent),
        title: "BANTADS - Autocadastro"
    },
    {
      path: 'cliente/dashboard',
      loadComponent: () => import('./features/cliente/screens/dashboard/dashboard').then(m => m.Dashboard),
      title: "BANTADS - Dashboard Cliente"
    },
    {
        path: 'admin/dashboard',
        loadComponent: () => import('./features/administrador/screens/dashbord-admin/dashbord-admin').then(m => m.DashboardAdminComponent),
        title: "BANTADS - Dashboard Administrador"
    },
    {
        path: 'admin/gerentes',
        loadComponent: () => import('./features/administrador/screens/crud-gerentes/crud-gerentes').then(m => m.CrudGerentesComponent),
        title: "BANTADS - Listar Gerentes"
    },
    {
        path: 'admin/relatorios',
        loadComponent: () => import('./features/administrador/screens/relatorio/relatorio').then(m => m.RelatorioComponent),
        title: "BANTADS - Relatórios"
    },
    {
        path: 'gerente/dashboard',
        loadComponent: () => import('./features/gerente/screens/dashboard-gerente/dashboard-gerente').then(m => m.DashboardGerente),
        title: "BANTADS - Dashboard Gerente"
    },

];
