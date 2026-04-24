package br.dac.bantads.ms_auth.infrastructure.security;

import br.dac.bantads.ms_auth.application.security.PasswordHasher;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class Sha256PasswordHasher implements PasswordHasher {

    private static final int SALT_LENGTH = 16; // Tamanho do SALT em bytes

    @Override
    public String hash(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        
        byte[] hash = computeHash(rawPassword, salt);
        
        // Armazenamos o salt junto ao hash separados por ":"
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || !encodedPassword.contains(":")) {
            return false;
        }
        
        String[] parts = encodedPassword.split(":");
        if (parts.length != 2) {
            return false;
        }
        
        try {
            // Recuperamos o salt e o hash esperado
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            String expectedHash = parts[1];
            
            // Calculamos o hash da senha atual usando o mesmo salt
            byte[] actualHash = computeHash(rawPassword, salt);
            String actualHashBase64 = Base64.getEncoder().encodeToString(actualHash);
            
            return expectedHash.equals(actualHashBase64);
        } catch (IllegalArgumentException e) {
            // Caso ocorra erro no decode do Base64
            return false;
        }
    }

    private byte[] computeHash(String rawPassword, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            return md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo SHA-256 não encontrado no sistema", e);
        }
    }
}
