package br.dac.bantads.ms_auth.infrastructure.messaging;

import br.dac.bantads.ms_auth.application.service.UserAccountService;
import br.dac.bantads.ms_auth.domain.account.AccountRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Listener para FILA_AUTENTICACAO — cria ou atualiza credenciais no ms-auth.
 *
 * Publicadores: ms-cliente (autocadastro/update), ms-funcionario (create/delete gerente),
 * ms-saga (passo 3 do fluxo de autocadastro orquestrado).
 *
 * Formato esperado: { "_id": "uuid", "email": "...", "senha": "...", "cargo": "CLIENTE|GERENTE|ADMINISTRADOR", "ativo": true/false, "sagaId": "uuid" (opcional) }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthAccountListener {

    private final UserAccountService userAccountService;
    private final ObjectMapper authObjectMapper;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.FILA_AUTENTICACAO)
    void processarAutenticacao(String msg) {
        log.info("Mensagem recebida em FILA_AUTENTICACAO: {}", msg);
        String sagaId = null;
        String email  = null;
        try {
            JsonNode node = authObjectMapper.readTree(msg);

            email  = node.path("email").asText(null);
            String senha  = node.path("senha").asText(null);
            String cargo  = node.path("cargo").asText("CLIENTE");
            boolean ativo = node.path("ativo").asBoolean(false);
            sagaId = node.path("sagaId").asText(null);
            if (sagaId != null && sagaId.isBlank()) sagaId = null;

            if (email == null || email.isBlank()) {
                log.warn("Mensagem sem campo 'email' ignorada: {}", msg);
                return;
            }

            AccountRole role = mapRole(cargo);

            if (ativo || senha != null && !senha.isBlank()) {
                // Criação ou atualização com senha fornecida
                userAccountService.createOrUpdateWithPassword(email, senha, role);
                log.info("Credencial criada/atualizada para: {} ({})", email, role);
            } else {
                // Exclusão / desativação (ex: gerente deletado envia ativo=false sem senha)
                try {
                    userAccountService.deleteAccountByEmail(email);
                    log.info("Credencial removida para: {}", email);
                } catch (Exception e) {
                    log.warn("Tentativa de remover credencial inexistente para: {}", email);
                }
            }

            publicarSucesso(sagaId, email);

        } catch (Exception e) {
            log.error("Erro ao processar FILA_AUTENTICACAO: {}", e.getMessage(), e);
            publicarErro(sagaId, e.getMessage());
        }
    }

    private void publicarSucesso(String sagaId, String email) {
        if (sagaId == null) return;
        try {
            String json = authObjectMapper.writeValueAsString(
                    java.util.Map.of("sagaId", sagaId, "sucesso", true, "mensagem", "auth criado para " + email)
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EVT_AUTH_CRIADO, json);
        } catch (Exception e) {
            log.error("Falha ao publicar SAGA_EVT_AUTH_CRIADO", e);
        }
    }

    private void publicarErro(String sagaId, String motivo) {
        if (sagaId == null) return;
        try {
            String json = authObjectMapper.writeValueAsString(
                    java.util.Map.of("sagaId", sagaId, "sucesso", false, "mensagem", motivo != null ? motivo : "erro no ms-auth")
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.SAGA_EVT_AUTH_ERRO, json);
        } catch (Exception e) {
            log.error("Falha ao publicar SAGA_EVT_AUTH_ERRO", e);
        }
    }

    private AccountRole mapRole(String cargo) {
        try {
            return AccountRole.fromValue(cargo);
        } catch (Exception e) {
            log.warn("Cargo desconhecido '{}', usando CLIENTE como padrão", cargo);
            return AccountRole.CLIENTE;
        }
    }
}
