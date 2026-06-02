package br.dac.bantads.ms_funcionario.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Security {
    
    private Security() {
        // Construtor privado para evitar instanciação de classe utilitária
    }

    public static String hash(String password) {
        if (password == null) {
            return null;
        }
        String finalPassword = null;
        String salt = "bantadsdoscrias";

        try {
            finalPassword = hashPassword(password, salt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return finalPassword;
    }

    private static byte[] digest(String password, String salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(salt.getBytes());
        return md.digest(password.getBytes());
    }

    private static String hashPassword(String password, String salt) throws NoSuchAlgorithmException {
        byte[] bytes = digest(password, salt);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
