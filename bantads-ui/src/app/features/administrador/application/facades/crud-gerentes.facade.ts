import { Injectable, computed, inject, signal } from '@angular/core';

import type { Gerente } from '../../domain/models/gerente.model';
import { GerenteAdminRepository } from '../../domain/repositories/gerente-admin.repository';

/**
 * Facade para o CRUD de Gerentes do administrador (R17–R20).
 *
 * Encapsula toda a lógica de distribuição de contas (R17) e
 * transferência na remoção (R18). Expõe lista reativa ordenada (R19).
 */
@Injectable()
export class CrudGerentesFacade {
  private readonly repository = inject(GerenteAdminRepository);

  private readonly _gerentes = signal<Gerente[]>([]);
  private readonly _feedback = signal<{ texto: string; erro: boolean } | null>(null);

  /**
   * Mapa em memória: gerenteId → quantidade de contas atreladas.
   * Inicializado a partir dos dados mock das imagens:
   * - Geniéve (id 1): Catharyna + Cutardo = 2 contas
   * - Godophredo (id 2): Cleuddônio + Coândrya = 2 contas
   * - Gyândula (id 3): Catianna = 1 conta
   */
  private contasPorGerente = new Map<number, number>([
    [1, 2],
    [2, 2],
    [3, 1],
  ]);

  /**
   * Mapa em memória: gerenteId → saldo positivo total.
   * Usado para desempate no R17.
   * - Geniéve: 800 (Catharyna) + 150.000 (Cutardo) = 150.800
   * - Godophredo: 1.500 (Coândrya) = 1.500 (Cleuddônio tem saldo negativo)
   * - Gyândula: 0 (Catianna tem saldo negativo)
   */
  private saldoPositivoPorGerente = new Map<number, number>([
    [1, 150800],
    [2, 1500],
    [3, 0],
  ]);

  readonly feedback = this._feedback.asReadonly();

  /** R19: Gerentes ordenados crescente por nome. */
  readonly gerentesOrdenados = computed(() => {
    const lista = this._gerentes();
    return [...lista].sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  });

  /** Quantidade de contas de um gerente. */
  contasDe(gerenteId: number): number {
    return this.contasPorGerente.get(gerenteId) ?? 0;
  }

  carregar(): void {
    this._gerentes.set(this.repository.listarTodos());
    this._feedback.set(null);
  }

  /**
   * R17: Inserção de Gerente.
   *
   * Ao inserir o gerente ele recebe automaticamente uma conta
   * do gerente que possui mais contas atreladas.
   * Se vários tiverem a mesma quantidade máxima, pega uma conta
   * do gerente com menor saldo positivo.
   * Se for o primeiro gerente, ou se só houver 1 gerente com 1 conta,
   * o novo gerente fica sem conta.
   */
  inserir(dados: Omit<Gerente, 'id'>): void {
    const gerente: Gerente = {
      ...dados,
      id: 0, // será gerado pelo repository
    };

    this.repository.inserir(gerente);

    const gerentesAtuais = this._gerentes();

    if (gerentesAtuais.length > 0) {
      // Encontra a quantidade máxima de contas
      let maxContas = 0;
      for (const g of gerentesAtuais) {
        const c = this.contasPorGerente.get(g.id) ?? 0;
        if (c > maxContas) maxContas = c;
      }

      // Candidatos: gerentes com quantidade máxima
      const candidatos = gerentesAtuais.filter(g => (this.contasPorGerente.get(g.id) ?? 0) === maxContas);

      // Regra de exceção: se só houver 1 gerente e ele tiver 1 conta, novo fica sem
      if (gerentesAtuais.length === 1 && maxContas <= 1) {
        // Novo gerente fica sem conta
      } else if (maxContas > 0) {
        // Desempate: gerente com menor saldo positivo
        let doador = candidatos[0];
        let menorSaldo = this.saldoPositivoPorGerente.get(doador.id) ?? 0;
        for (const c of candidatos) {
          const s = this.saldoPositivoPorGerente.get(c.id) ?? 0;
          if (s < menorSaldo) {
            menorSaldo = s;
            doador = c;
          }
        }

        // Transfere uma conta
        const contasDoador = this.contasPorGerente.get(doador.id) ?? 0;
        this.contasPorGerente.set(doador.id, contasDoador - 1);

        // Novo gerente obtém o ID real do repositório
        const novosGerentes = this.repository.listarTodos();
        const novoGerente = novosGerentes.find(g => !gerentesAtuais.some(a => a.id === g.id));
        if (novoGerente) {
          this.contasPorGerente.set(novoGerente.id, 1);
          this.saldoPositivoPorGerente.set(novoGerente.id, 0);
        }
      }
    }

    this._gerentes.set(this.repository.listarTodos());
    this._feedback.set({ texto: 'Gerente cadastrado com sucesso!', erro: false });
  }

  /**
   * R18: Remoção de Gerente.
   *
   * As contas que ele mantém devem ser atribuídas para o gerente que possua
   * menos contas atreladas. Não permitir remoção do último gerente.
   */
  remover(id: number): { sucesso: boolean; mensagem: string } {
    const gerentesAtuais = this._gerentes();

    if (gerentesAtuais.length <= 1) {
      this._feedback.set({ texto: 'Não é permitido remover o último gerente do banco.', erro: true });
      return { sucesso: false, mensagem: 'Não é permitido remover o último gerente do banco.' };
    }

    const gerenteRemovido = gerentesAtuais.find(g => g.id === id);
    if (!gerenteRemovido) {
      this._feedback.set({ texto: 'Gerente não encontrado.', erro: true });
      return { sucesso: false, mensagem: 'Gerente não encontrado.' };
    }

    const contasRemovido = this.contasPorGerente.get(id) ?? 0;
    const outrosGerentes = gerentesAtuais.filter(g => g.id !== id);

    if (contasRemovido > 0 && outrosGerentes.length > 0) {
      // Encontra gerente com menos contas
      let recebedor = outrosGerentes[0];
      let minContas = this.contasPorGerente.get(recebedor.id) ?? 0;
      for (const g of outrosGerentes) {
        const c = this.contasPorGerente.get(g.id) ?? 0;
        if (c < minContas) {
          minContas = c;
          recebedor = g;
        }
      }

      // Transfere todas as contas
      this.contasPorGerente.set(recebedor.id, minContas + contasRemovido);
    }

    this.contasPorGerente.delete(id);
    this.saldoPositivoPorGerente.delete(id);
    this.repository.remover(id);
    this._gerentes.set(this.repository.listarTodos());

    const msg = contasRemovido > 0
      ? `Gerente excluído. ${contasRemovido} conta(s) transferida(s).`
      : 'Gerente excluído com sucesso.';
    this._feedback.set({ texto: msg, erro: false });
    return { sucesso: true, mensagem: msg };
  }

  /**
   * R20: Alteração de Gerente — somente nome, e-mail e senha.
   */
  atualizar(id: number, dados: { nome: string; email: string; senha?: string }): void {
    const gerente = this.repository.buscarPorId(id);
    if (!gerente) {
      this._feedback.set({ texto: 'Gerente não encontrado.', erro: true });
      return;
    }

    const atualizado: Gerente = {
      ...gerente,
      nome: dados.nome,
      email: dados.email,
      senha: dados.senha ?? gerente.senha,
    };

    this.repository.atualizar(atualizado);
    this._gerentes.set(this.repository.listarTodos());
    this._feedback.set({ texto: 'Dados do gerente atualizados com sucesso!', erro: false });
  }

  limparFeedback(): void {
    this._feedback.set(null);
  }
}
