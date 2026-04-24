package br.dac.bantads.ms_auth.application.service;

import br.dac.bantads.ms_auth.application.event.RandomPasswordGeneratedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationListener {

    private final JavaMailSender mailSender;

    public EmailNotificationListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @EventListener
    public void handleRandomPasswordGeneratedEvent(RandomPasswordGeneratedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("albanoaws2024@gmail.com");
        message.setTo(event.email());
        message.setSubject("BANTADS - Sua senha de acesso");
        message.setText("Sua conta foi aprovada! Sua senha temporária de acesso é: " + event.password());
        
        mailSender.send(message);
    }
}
