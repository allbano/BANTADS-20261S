import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessaoService } from '../services/sessao.service';

export const clienteLogadoGuard: CanActivateFn = () => {
  const sessao = inject(SessaoService);
  const router = inject(Router);
  if (sessao.tipo() === 'CLIENTE') {
    return true;
  }
  return router.createUrlTree(['/auth/login']);
};
