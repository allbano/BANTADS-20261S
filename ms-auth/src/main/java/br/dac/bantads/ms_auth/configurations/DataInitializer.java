package br.dac.bantads.ms_auth.configurations;

import br.dac.bantads.ms_auth.models.User;
import br.dac.bantads.ms_auth.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            List<User> users = List.of(
                criarUsuario("cli1@bantads.com.br", "Catharyna", "tads", "CLIENTE", "12912861012", "ATIVO", now),
                criarUsuario("cli2@bantads.com.br", "Cleuddônio", "tads", "CLIENTE", "09506382000", "ATIVO", now),
                criarUsuario("cli3@bantads.com.br", "Catianna", "tads", "CLIENTE", "85733854057", "ATIVO", now),
                criarUsuario("cli4@bantads.com.br", "Cutardo", "tads", "CLIENTE", "58872160006", "ATIVO", now),
                criarUsuario("cli5@bantads.com.br", "Coândrya", "tads", "CLIENTE", "76179646090", "ATIVO", now),
                criarUsuario("ger1@bantads.com.br", "Geniéve", "tads", "GERENTE", "98574307084", "ATIVO", now),
                criarUsuario("ger2@bantads.com.br", "Godophredo", "tads", "GERENTE", "64065268052", "ATIVO", now),
                criarUsuario("ger3@bantads.com.br", "Gyândula", "tads", "GERENTE", "23862179060", "ATIVO", now),
                criarUsuario("adm1@bantads.com.br", "Adamântio", "tads", "ADMINISTRADOR", "40501740066", "ATIVO", now)
            );

            userRepository.saveAll(users);
            System.out.println("Seed: " + users.size() + " usuarios inseridos no db_auth.");
        };
    }

    private User criarUsuario(String email, String nome, String senha, String tipo, String cpf, String status, LocalDateTime dataCriacao) {
        String salt = gerarSalt();
        String senhaHash = hashSenha(senha, salt);

        User user = new User();
        user.setEmail(email);
        user.setNome(nome);
        user.setSenhaHash(senhaHash);
        user.setSalt(salt);
        user.setTipo(tipo);
        user.setCpfRef(cpf);
        user.setStatus(status);
        user.setDataCriacao(dataCriacao);
        return user;
    }

    private String gerarSalt() {
        byte[] saltBytes = new byte[16];
        new SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    private String hashSenha(String senha, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((salt + senha).getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }
}
