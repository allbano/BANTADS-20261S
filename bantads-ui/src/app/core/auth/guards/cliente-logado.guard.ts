import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessaoClienteService } from '../services/sessao-cliente.service';

export const clienteLogadoGuard: CanActivateFn = () => {
  const sessao = inject(SessaoClienteService);
  const router = inject(Router);
  if (sessao.clienteId() !== null) {
    return true;
  }
  return router.createUrlTree(['/auth/login']);
};
