package br.dac.bantads.ms_auth.application.security;

public interface PasswordHasher {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
