package br.dac.bantads.ms_auth.application.ports;

public interface PasswordHasher {
    String sha256(String value);
}
