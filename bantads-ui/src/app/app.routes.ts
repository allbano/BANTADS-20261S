import { Routes } from '@angular/router';

import { DashbordAdmin } from './features/administrador/pages/dashbord-admin/dashbord-admin';
import { CrudGerentes } from './features/administrador/pages/crud-gerentes/crud-gerentes';
import { Relatorio } from './features/administrador/pages/relatorio/relatorio';

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
    },
    {
        path: 'admin/dashboard',
        // Atenção ao nome da pasta "dashbord-admin" (sem o 'a') que está nos seus ficheiros
        loadComponent: () => import('./features/administrador/pages/dashbord-admin/dashbord-admin').then(m => m.DashbordAdmin)
    },
    {
        path: 'admin/gerentes',
        loadComponent: () => import('./features/administrador/pages/crud-gerentes/crud-gerentes').then(m => m.CrudGerentes)
    },
    {
        path: 'admin/relatorios',
        loadComponent: () => import('./features/administrador/pages/relatorio/relatorio').then(m => m.Relatorio)
    }
    
    
];
