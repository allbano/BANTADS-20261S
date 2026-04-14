package br.dac.bantads.ms_auth.application.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciais invalidas");
    }
}
