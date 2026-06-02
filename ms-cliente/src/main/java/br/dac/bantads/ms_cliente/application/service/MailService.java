package br.dac.bantads.ms_cliente.application.service;

public interface MailService {
    void sendMail(String to, String subject, String text);
}
