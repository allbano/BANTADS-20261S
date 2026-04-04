import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { SessaoAdminService } from '../services/sessao-admin.service';

export const adminLogadoGuard: CanActivateFn = () => {
  const sessao = inject(SessaoAdminService);
  const router = inject(Router);
  if (sessao.adminId() !== null) {
    return true;
  }
  return router.createUrlTree(['/auth/login']);
};
