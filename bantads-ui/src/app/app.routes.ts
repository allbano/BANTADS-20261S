import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./features/public/home/home').then(m => m.Home)
    },
    {
        path: 'dashboard-gerente',
        loadComponent: () => import('./features/gerente/pages/dashboard-gerente/dashboard-gerente').then(m => m.DashboardGerente)
    },
    {
        path: 'clientes/autocadastro',
        loadComponent: () => import('./features/clientes/pages/autocadastro/autocadastro').then(m => m.AutocadastroComponent)
    }
];
