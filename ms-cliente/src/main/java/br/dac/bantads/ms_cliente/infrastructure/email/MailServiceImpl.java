package br.dac.bantads.ms_cliente.infrastructure.email;

import br.dac.bantads.ms_cliente.application.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender sender;

    /**
     * Envio de e-mail nao-fatal: uma falha de SMTP (ex.: credencial invalida)
     * apenas registra um aviso e NUNCA propaga, para nao derrubar o fluxo de
     * negocio nem o passo de uma SAGA. Notificacao e efeito colateral.
     */
    @Override
    public void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("bantads.dac777@gmail.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            sender.send(message);
        } catch (Exception e) {
            log.warn("Falha ao enviar e-mail para {} (assunto '{}'): {}. Fluxo continua.",
                    to, subject, e.getMessage());
        }
    }
}
