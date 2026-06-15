import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

import { SessaoService } from '../auth/services/sessao.service';
import { environment } from '../../../environments/environment';

/**
 * Extrai uma mensagem legível do corpo de erro da API Gateway.
 * A Gateway responde em formatos variados ({message}, {error}, string).
 */
export function mensagemDeErro(err: HttpErrorResponse, padrao = 'Ocorreu um erro inesperado.'): string {
  const corpo = err.error;
  if (typeof corpo === 'string' && corpo.trim()) {
    return corpo;
  }
  if (corpo && typeof corpo === 'object') {
    return corpo.message ?? corpo.error ?? corpo.mensagem ?? padrao;
  }
  return padrao;
}

/**
 * Em 401 (token ausente/expirado) numa chamada à Gateway, encerra a sessão e
 * redireciona ao login. Demais erros são propagados para as facades tratarem.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const sessao = inject(SessaoService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const ehChamadaGateway = req.url.startsWith(environment.apiUrl);
      const ehLogin = req.url.includes('/login');
      if (ehChamadaGateway && err.status === 401 && !ehLogin) {
        sessao.encerrar();
        void router.navigateByUrl('/auth/login');
      }
      return throwError(() => err);
    }),
  );
};
