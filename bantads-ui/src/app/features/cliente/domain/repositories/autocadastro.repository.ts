import { Observable } from 'rxjs';
import { AutocadastroPayload } from '../models/autocadastro.model';

export abstract class AutocadastroRepository {
  /**
   * Envia a solicitação de autocadastro na API.
   * Dependendo do design, pode retornar `void` ou `boolean` se for fire and forget,
   * mas usando Observable mantemos flexibilidade.
   */
  abstract solicitarCadastro(payload: AutocadastroPayload): Observable<void>;
}
