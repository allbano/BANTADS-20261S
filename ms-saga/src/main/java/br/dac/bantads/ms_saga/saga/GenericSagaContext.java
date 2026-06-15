package br.dac.bantads.ms_saga.saga;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Estado em memória de uma instância de SAGA genérica (suficiente para o
 * contexto acadêmico). Guarda o tipo, a definição ordenada de passos, o índice
 * do passo corrente e um mapa de dados acumulados — semeado com a requisição
 * inicial e enriquecido pelos {@code dados} de cada resposta de passo.
 */
public class GenericSagaContext {

    private final UUID sagaId;
    private final SagaTipo tipo;
    private final List<SagaStep> passos;
    private final Map<String, Object> dados = new ConcurrentHashMap<>();

    private int passoAtual = 0;
    private SagaStatus status = SagaStatus.INICIADA;

    public GenericSagaContext(UUID sagaId, SagaTipo tipo, List<SagaStep> passos, Map<String, Object> dadosIniciais) {
        this.sagaId = sagaId;
        this.tipo = tipo;
        this.passos = passos;
        if (dadosIniciais != null) {
            dadosIniciais.forEach((k, v) -> { if (v != null) dados.put(k, v); });
        }
    }

    public UUID getSagaId()        { return sagaId; }
    public SagaTipo getTipo()      { return tipo; }
    public List<SagaStep> getPassos() { return passos; }
    public Map<String, Object> getDados() { return dados; }

    public int getPassoAtual()     { return passoAtual; }
    public void avancar()          { this.passoAtual++; }
    public boolean concluido()     { return passoAtual >= passos.size(); }
    public SagaStep passoCorrente(){ return passos.get(passoAtual); }

    public SagaStatus getStatus()  { return status; }
    public void setStatus(SagaStatus status) { this.status = status; }

    public void mesclarDados(Map<String, Object> novos) {
        if (novos != null) novos.forEach((k, v) -> { if (v != null) dados.put(k, v); });
    }
}
