import { Injectable } from '@angular/core';
import { Observable, timer } from 'rxjs';
import { map } from 'rxjs/operators';
import { AutocadastroRepository } from '../../domain/repositories/autocadastro.repository';
import { AutocadastroPayload } from '../../domain/models/autocadastro.model';

@Injectable({
  providedIn: 'root'
})
export class AutocadastroApiService implements AutocadastroRepository {

  solicitarCadastro(payload: AutocadastroPayload): Observable<void> {
    console.log('[API] Fire and Forget: Enviando solicitação de autocadastro...', payload);
    
    // Simula uma chamada API com o backend
    return timer(1500).pipe(
      map(() => void 0)
    );
  }
}
