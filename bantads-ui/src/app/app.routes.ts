import { Routes } from '@angular/router';

import { clienteLogadoGuard } from './core/auth/guards/cliente-logado.guard';
import { gerenteLogadoGuard } from './core/auth/guards/gerente-logado.guard';
import { adminLogadoGuard } from './core/auth/guards/admin-logado.guard';
import { Home } from './features/public/home/home';

export const routes: Routes = [
    {
        path: '',
        component: Home,
        title: "BANTADS - Banco do TADS"
    },
    {
      path: 'auth/login',
      loadComponent: () => import('./core/auth/screens/login/login').then(m => m.Login),
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
      canActivate: [clienteLogadoGuard],
      title: "BANTADS - Dashboard Cliente"
    },
    {
      path: 'cliente/deposito',
      loadComponent: () => import('./features/cliente/screens/deposito/deposito').then(m => m.Deposito),
      canActivate: [clienteLogadoGuard],
      title: "BANTADS - Depósito",
    },
    {
      path: 'cliente/saque',
      loadComponent: () => import('./features/cliente/screens/saque/saque').then(m => m.Saque),
      canActivate: [clienteLogadoGuard],
      title: "BANTADS - Saque",
    },
    {
      path: 'cliente/transferencia',
      loadComponent: () => import('./features/cliente/screens/transferencia/transferencia').then(m => m.Transferencia),
      canActivate: [clienteLogadoGuard],
      title: "BANTADS - Transferência",
    },
    {
      path: 'cliente/extrato',
      loadComponent: () => import('./features/cliente/screens/extrato/extrato').then(m => m.Extrato),
      canActivate: [clienteLogadoGuard],
      title: "BANTADS - Extrato",
    },
    {
      path: 'cliente/perfil',
      loadComponent: () => import('./features/cliente/screens/meu-perfil/meu-perfil').then(m => m.MeuPerfil),
      canActivate: [clienteLogadoGuard],
      title: "BANTADS - Meu perfil",
    },
    {
        path: 'admin/dashboard',
        loadComponent: () => import('./features/administrador/screens/dashbord-admin/dashbord-admin').then(m => m.DashboardAdminComponent),
        canActivate: [adminLogadoGuard],
        title: "BANTADS - Dashboard Administrador"
    },
    {
        path: 'admin/gerentes',
        loadComponent: () => import('./features/administrador/screens/crud-gerentes/crud-gerentes').then(m => m.CrudGerentesComponent),
        canActivate: [adminLogadoGuard],
        title: "BANTADS - Listar Gerentes"
    },
    {
        path: 'admin/relatorios',
        loadComponent: () => import('./features/administrador/screens/relatorio/relatorio').then(m => m.RelatorioComponent),
        canActivate: [adminLogadoGuard],
        title: "BANTADS - Relatórios"
    },
    {
        path: 'gerente/dashboard',
        loadComponent: () => import('./features/gerente/screens/dashboard-gerente/dashboard-gerente').then(m => m.DashboardGerente),
        canActivate: [gerenteLogadoGuard],
        title: "BANTADS - Aprovações Pendentes"
    },
    {
        path: 'gerente/clientes',
        loadComponent: () => import('./features/gerente/screens/clientes-gerente/clientes-gerente').then(m => m.ClientesGerente),
        canActivate: [gerenteLogadoGuard],
        title: "BANTADS - Clientes do Gerente"
    },
    {
        path: 'gerente/clientes/:id',
        loadComponent: () => import('./features/gerente/screens/detalhe-cliente/detalhe-cliente').then(m => m.DetalheCliente),
        canActivate: [gerenteLogadoGuard],
        title: "BANTADS - Detalhe do Cliente"
    },
    {
        path: 'gerente/consulta',
        loadComponent: () => import('./features/gerente/screens/consulta-cliente/consulta-cliente').then(m => m.ConsultaCliente),
        canActivate: [gerenteLogadoGuard],
        title: "BANTADS - Consulta por CPF"
    },
    {
        path: 'gerente/top-clientes',
        loadComponent: () => import('./features/gerente/screens/top-clientes/top-clientes').then(m => m.TopClientes),
        canActivate: [gerenteLogadoGuard],
        title: "BANTADS - Top 3 Clientes"
    },

];
