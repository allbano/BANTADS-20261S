package br.dac.bantads.ms_saga.saga;

/**
 * Routing keys do mecanismo genérico de SAGA (Eixo 3).
 *
 * Todas as SAGAs novas (Aprovar R10, Perfil R4, Inserir Gerente R17,
 * Remover Gerente R18, Alterar Gerente R20) usam um canal de resposta único
 * {@link #REPLY} no Topic Exchange {@code bantads.topic}. Cada participante
 * consome o(s) comando(s) que possui e responde sempre em {@link #REPLY} com
 * {@code {sagaId, sucesso, mensagem, dados{}}} (String JSON crua — ver contrato
 * de mensageria). Estes literais DEVEM ser idênticos aos declarados nos demais MS.
 */
public final class SagaRoutes {

    private SagaRoutes() {}

    public static final String EXCHANGE = "bantads.topic";

    /** Canal único de resposta dos passos (consumido só pelo ms-saga). */
    public static final String REPLY = "saga.reply";

    // ── Comandos ms-cliente ──
    public static final String CLIENTE_APROVAR   = "saga.cmd.cliente.aprovar";
    public static final String CLIENTE_REPROVAR  = "saga.cmd.cliente.reprovar";   // compensação
    public static final String CLIENTE_ATUALIZAR = "saga.cmd.cliente.atualizar";
    public static final String CLIENTE_RESTAURAR = "saga.cmd.cliente.restaurar";  // compensação

    // ── Comandos ms-conta ──
    public static final String CONTA_ATIVAR            = "saga.cmd.conta.ativar";
    public static final String CONTA_DESATIVAR         = "saga.cmd.conta.desativar";          // compensação
    public static final String CONTA_RECALC_LIMITE     = "saga.cmd.conta.recalcular.limite";
    public static final String CONTA_ATRIBUIR_GERENTE  = "saga.cmd.conta.atribuir.gerente";
    public static final String CONTA_REDISTRIBUIR      = "saga.cmd.conta.redistribuir.gerente";

    // ── Comandos ms-funcionario ──
    public static final String GERENTE_INSERIR        = "saga.cmd.gerente.inserir";
    public static final String GERENTE_EXCLUIR        = "saga.cmd.gerente.excluir";          // também compensação do inserir
    public static final String GERENTE_VALIDAR_REMOCAO = "saga.cmd.gerente.validar.remocao";
    public static final String GERENTE_ALTERAR        = "saga.cmd.gerente.alterar";

    // ── Comandos ms-auth ──
    public static final String AUTH_CRIAR          = "saga.cmd.auth.criar";
    public static final String AUTH_REMOVER        = "saga.cmd.auth.remover";         // também compensação do criar
    public static final String AUTH_GERAR_SENHA    = "saga.cmd.auth.gerar.senha";
    public static final String AUTH_ATUALIZAR_SENHA = "saga.cmd.auth.atualizar.senha";

    // ── Comando ms-notificacao ──
    public static final String NOTIFICAR_CLIENTE = "saga.cmd.notificar.cliente";
}
