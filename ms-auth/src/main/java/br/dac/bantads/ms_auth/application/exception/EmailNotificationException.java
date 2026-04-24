package br.dac.bantads.ms_auth.application.exception;

public class EmailNotificationException extends RuntimeException {
    public EmailNotificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
