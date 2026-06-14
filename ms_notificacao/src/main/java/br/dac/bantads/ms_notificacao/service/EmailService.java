package br.dac.bantads.ms_notificacao.service;

import br.dac.bantads.ms_notificacao.dto.NotificacaoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servico responsavel por montar e enviar o e-mail correto para cada tipo de
 * notificacao. Usa o {@link JavaMailSender} autoconfigurado pelo Spring a
 * partir das propriedades {@code spring.mail.*}.
 */
@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /** Endereco remetente (FROM), lido de {@code spring.mail.username}. */
    private final String remetente;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    /**
     * Envio genérico {destino, assunto, mensagem}: é o ponto único pelo qual os
     * demais microsserviços solicitam e-mails (publicando na fila), de modo que
     * SOMENTE o ms_notificacao fale com o SMTP. O corpo já vem pronto do produtor.
     */
    public void enviarSimples(String destino, String assunto, String mensagem) {
        enviarEmail(destino, assunto, mensagem);
    }

    /**
     * Ponto de entrada: recebe a notificacao ja desserializada, monta o
     * assunto/corpo conforme o {@code tipo} e envia o e-mail.
     *
     * Lanca excecao se o tipo for nulo/desconhecido; o consumidor decide o que
     * fazer com a falha (logar, descartar ou reenfileirar).
     */
    public void enviar(NotificacaoDTO n) {
        if (n.getTipo() == null) {
            throw new IllegalArgumentException("Notificacao sem 'tipo' definido");
        }
        switch (n.getTipo()) {
            // RF10 - Aprovacao de cliente: e-mail com a senha de acesso gerada.
            case APROVACAO -> enviarEmail(n.getEmail(),
                    "BANTADS - Cadastro aprovado",
                    saudacao(n.getNome())
                            + "Sua conta foi aprovada com sucesso!\n"
                            + "Sua senha de acesso e: " + n.getSenha() + "\n\n"
                            + "Recomendamos alterar a senha apos o primeiro acesso.");

            // RF11 - Rejeicao de cliente: e-mail com o motivo da recusa.
            case REJEICAO -> enviarEmail(n.getEmail(),
                    "BANTADS - Cadastro nao aprovado",
                    saudacao(n.getNome())
                            + "Infelizmente sua solicitacao de cadastro nao foi aprovada.\n"
                            + "Motivo: " + n.getMotivo());

            // RF01 - Falha no autocadastro (compensacao da SAGA): solicitacao nao efetuada.
            case FALHA_AUTOCADASTRO -> enviarEmail(n.getEmail(),
                    "BANTADS - Solicitacao nao efetuada",
                    saudacao(n.getNome())
                            + "Nao foi possivel concluir sua solicitacao de cadastro.\n"
                            + "Por favor, tente novamente mais tarde.");
        }
    }

    /**
     * Monta uma saudacao personalizada quando o nome esta disponivel;
     * caso contrario usa uma saudacao generica.
     */
    private String saudacao(String nome) {
        return (nome != null && !nome.isBlank())
                ? "Ola, " + nome + "!\n\n"
                : "Ola!\n\n";
    }

    /**
     * Efetivamente envia o e-mail de texto simples via SMTP.
     * Centraliza a criacao do {@link SimpleMailMessage} para nao repetir codigo.
     */
    private void enviarEmail(String destino, String assunto, String corpo) {
        if (destino == null || destino.isBlank()) {
            throw new IllegalArgumentException("Notificacao sem 'email' de destino");
        }
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destino);
        mensagem.setSubject(assunto);
        mensagem.setText(corpo);
        mailSender.send(mensagem);
        log.info("E-mail '{}' enviado para {}", assunto, destino);
    }
}
