package br.com.sosviale.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/*
 * Utilitário para hash e verificação segura de senhas usando PBKDF2 puro do JDK
 * zero dependências externas, garantindo compatibilidade e facilidade para a equipe
 */
public final class PasswordUtil {

    // Recomendação atual de segurança para PBKDF2-HMAC-SHA256
    private static final int ITERACOES = 310000;
    private static final int TAMANHO_CHAVE = 256;
    private static final int TAMANHO_SALT = 16;
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";

    private PasswordUtil() {
        // Classe utilitária
    }

    public static String hashSenha(String senhaTextoPlano) {
        if (senhaTextoPlano == null || senhaTextoPlano.isEmpty()) {
            throw new IllegalArgumentException("Senha não pode ser nula ou vazia.");
        }
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[TAMANHO_SALT];
            random.nextBytes(salt);

            byte[] hash = pbkdf2(senhaTextoPlano.toCharArray(), salt, ITERACOES);

            // Formato salvo no banco: iteracoes:saltBase64:hashBase64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hash);

            return ITERACOES + ":" + saltBase64 + ":" + hashBase64;
        } catch (Exception e) {
            throw new RuntimeException("Erro interno ao gerar hash da senha", e);
        }
    }

    public static boolean verificarSenha(String senhaTextoPlano, String hashArmazenado) {
        if (senhaTextoPlano == null || hashArmazenado == null) {
            return false;
        }

        String[] partes = hashArmazenado.split(":");
        if (partes.length != 3) {
            return false; // Senha em texto puro (legado) ou formato inválido
        }

        try {
            int iteracoes = Integer.parseInt(partes[0]);
            byte[] salt = Base64.getDecoder().decode(partes[1]);
            byte[] hashReal = Base64.getDecoder().decode(partes[2]);

            byte[] hashTeste = pbkdf2(senhaTextoPlano.toCharArray(), salt, iteracoes);

            return comparacaoTempoConstante(hashReal, hashTeste);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean precisaReHash(String hashArmazenado) {
        if (hashArmazenado == null || !hashArmazenado.contains(":")) {
            return true; // Dado legado (provavelmente texto puro), precisa de hash urgente
        }
        try {
            String[] partes = hashArmazenado.split(":");
            int iteracoes = Integer.parseInt(partes[0]);
            return iteracoes < ITERACOES;
        } catch (Exception e) {
            return true;
        }
    }

    private static byte[] pbkdf2(char[] senha, byte[] salt, int iteracoes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(senha, salt, iteracoes, TAMANHO_CHAVE);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITMO);
        return skf.generateSecret(spec).getEncoded();
    }

    // Evita timing attacks (Ataques baseados no tempo de resposta)
    private static boolean comparacaoTempoConstante(byte[] a, byte[] b) {
        int diferenca = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diferenca |= a[i] ^ b[i];
        }
        return diferenca == 0;
    }
}