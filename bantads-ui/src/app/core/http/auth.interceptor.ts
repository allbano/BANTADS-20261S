import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';

import { SessaoService } from '../auth/services/sessao.service';
import { environment } from '../../../environments/environment';

/**
 * Adiciona o cabeçalho Authorization: Bearer <token> nas chamadas à API Gateway.
 * Não toca em requisições externas (ex.: ViaCEP) nem em rotas públicas (login/reboot/autocadastro).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const sessao = inject(SessaoService);

  const ehChamadaGateway = req.url.startsWith(environment.apiUrl);
  if (!ehChamadaGateway) {
    return next(req);
  }

  const token = sessao.token();
  if (!token) {
    return next(req);
  }

  const autenticada = req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
  });
  return next(autenticada);
};
