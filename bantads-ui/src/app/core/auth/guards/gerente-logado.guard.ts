import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessaoGerenteService } from '../services/sessao-gerente.service';

export const gerenteLogadoGuard: CanActivateFn = () => {
  const sessao = inject(SessaoGerenteService);
  const router = inject(Router);
  if (sessao.gerenteId() !== null) {
    return true;
  }
  return router.createUrlTree(['/auth/login']);
};
