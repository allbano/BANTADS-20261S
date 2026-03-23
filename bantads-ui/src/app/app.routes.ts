import { Routes } from '@angular/router';

//Publicas

//Clientes
import { Dashboard } from './features/clientes/pages/dashboard/dashboard';

//Gerentes
import { DashboardGerente } from './features/gerente/pages/dashboard-gerente/dashboard-gerente'

//Admins

export const routes: Routes = [
    {
        path: '',
        component: Dashboard
    },
    {
        path: 'dashboard-gerente',
        component: DashboardGerente
    },
];
