package br.dac.bantads.ms_saga.saga;

/**
 * Um passo de uma SAGA orquestrada: a routing key do comando a publicar e,
 * opcionalmente, a routing key da compensação (executada em ordem inversa se
 * um passo posterior falhar). {@code compensacao == null} ⇒ passo sem desfazer.
 */
public record SagaStep(String comando, String compensacao) {

    public static SagaStep of(String comando, String compensacao) {
        return new SagaStep(comando, compensacao);
    }

    public static SagaStep of(String comando) {
        return new SagaStep(comando, null);
    }
}
