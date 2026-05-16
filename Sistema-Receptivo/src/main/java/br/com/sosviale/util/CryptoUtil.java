package br.com.sosviale.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/*
 * Criptografia autenticada AES-256-GCM para dados sensíveis (ex: passaportes).
 *
 * POR QUE AES-GCM e não AES-CBC?
 *   - GCM (Galois/Counter Mode) é criptografia AUTENTICADA: além de cifrar,
 *     gera uma tag de autenticação (MAC) de 128 bits.
 *   - Se alguém alterar o dado criptografado no banco, a descriptografia
 *     lança uma exceção antes de retornar qualquer dado — proteção contra
 *     tampering (adulteração).
 *   - CBC sem MAC é vulnerável a padding oracle attacks.
 *
 * COMO A CHAVE É DERIVADA:
 *   - Usamos PBKDF2-HMAC-SHA256 com 310.000 iterações (recomendação OWASP 2024)
 *     para derivar uma chave AES-256 a partir de uma senha mestra.
 *   - A "senha mestra" deve vir de variável de ambiente ou arquivo de config
 *     fora do repositório (ex: /etc/receptivo/secrets.properties).
 *
 * FORMATO DO DADO CIFRADO (Base64):
 *   [ salt (16 bytes) | IV (12 bytes) | ciphertext + GCM tag (variável) ]
 *   Tudo concatenado e codificado em Base64 para salvar no banco como texto.
 */
public final class CryptoUtil {

    // ── Constantes de configuração ──────────────────────────────────────────
    private static final String ALGORITHM        = "AES/GCM/NoPadding";
    private static final String KEY_FACTORY      = "PBKDF2WithHmacSHA256";
    private static final int    KEY_SIZE_BITS    = 256;
    private static final int    GCM_TAG_BITS     = 128;
    private static final int    GCM_IV_BYTES     = 12;   // 96 bits — padrão NIST para GCM
    private static final int    SALT_BYTES       = 16;
    private static final int    PBKDF2_ITERS     = 310_000; // OWASP 2024

    // ── Variável de ambiente que contém a senha mestra ──────────────────────
    private static final String ENV_KEY = "RECEPTIVO_CRYPTO_KEY";

    // ── Dados de contexto para AAD (Additional Authenticated Data) ──────────
    // AAD vincula o dado cifrado ao seu contexto (tabela + coluna).
    // Impede que um atacante mova um blob cifrado de uma coluna para outra.
    private static final String AAD_PASSAPORTE = "passageiros.documento.passaporte";
    private static final String AAD_DOCUMENTO  = "passageiros.documento.generico";

    private CryptoUtil() {}

    // ════════════════════════════════════════════════════════════════════════
    //  API PÚBLICA
    // ════════════════════════════════════════════════════════════════════════

    /*
     * Criptografa um número de documento sensível.
     *
     * @param documentoTextoPlano Número do passaporte ou documento a proteger
     * @param isPassaporte        true = AAD de passaporte; false = genérico
     * @return String Base64 pronta para armazenar no banco
     */
    public static String encrypt(String documentoTextoPlano, boolean isPassaporte) {
        if (documentoTextoPlano == null || documentoTextoPlano.isBlank()) {
            return documentoTextoPlano;
        }
        try {
            byte[] salt = gerarBytes(SALT_BYTES);
            byte[] iv   = gerarBytes(GCM_IV_BYTES);

            SecretKey chave = derivarChave(obterSenhaMestra(), salt);
            Cipher cipher   = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, chave, new GCMParameterSpec(GCM_TAG_BITS, iv));
            cipher.updateAAD(aad(isPassaporte));

            byte[] ciphertext = cipher.doFinal(documentoTextoPlano.getBytes(StandardCharsets.UTF_8));

            // Concatena: salt | iv | ciphertext+tag
            byte[] resultado = new byte[SALT_BYTES + GCM_IV_BYTES + ciphertext.length];
            System.arraycopy(salt,       0, resultado, 0,                        SALT_BYTES);
            System.arraycopy(iv,         0, resultado, SALT_BYTES,               GCM_IV_BYTES);
            System.arraycopy(ciphertext, 0, resultado, SALT_BYTES + GCM_IV_BYTES, ciphertext.length);

            return Base64.getEncoder().encodeToString(resultado);

        } catch (Exception e) {
            throw new CryptoException("Falha ao criptografar documento.", e);
        }
    }

    /*
     * Descriptografa um número de documento previamente cifrado.
     *
     * @param base64Cifrado   Valor cifrado vindo do banco
     * @param isPassaporte    Deve coincidir com o flag usado no encrypt
     * @return Número do documento em texto plano
     * @throws CryptoException se o dado foi adulterado ou a chave está errada
     */
    public static String decrypt(String base64Cifrado, boolean isPassaporte) {
        if (base64Cifrado == null || base64Cifrado.isBlank()) {
            return base64Cifrado;
        }
        // Dado legado (texto puro, sem prefixo Base64 válido de bloco cifrado)
        if (isLegado(base64Cifrado)) {
            return base64Cifrado;
        }
        try {
            byte[] raw = Base64.getDecoder().decode(base64Cifrado);

            byte[] salt       = new byte[SALT_BYTES];
            byte[] iv         = new byte[GCM_IV_BYTES];
            byte[] ciphertext = new byte[raw.length - SALT_BYTES - GCM_IV_BYTES];

            System.arraycopy(raw, 0,                        salt,       0, SALT_BYTES);
            System.arraycopy(raw, SALT_BYTES,               iv,         0, GCM_IV_BYTES);
            System.arraycopy(raw, SALT_BYTES + GCM_IV_BYTES, ciphertext, 0, ciphertext.length);

            SecretKey chave = derivarChave(obterSenhaMestra(), salt);
            Cipher cipher   = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, chave, new GCMParameterSpec(GCM_TAG_BITS, iv));
            cipher.updateAAD(aad(isPassaporte));

            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new CryptoException("Falha ao descriptografar documento. " +
                    "O dado pode ter sido adulterado ou a chave está incorreta.", e);
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MÉTODOS PRIVADOS
    // ════════════════════════════════════════════════════════════════════════

    private static SecretKey derivarChave(char[] senha, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY);
        KeySpec spec = new PBEKeySpec(senha, salt, PBKDF2_ITERS, KEY_SIZE_BITS);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    private static byte[] gerarBytes(int tamanho) {
        byte[] bytes = new byte[tamanho];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    private static byte[] aad(boolean isPassaporte) {
        String ctx = isPassaporte ? AAD_PASSAPORTE : AAD_DOCUMENTO;
        return ctx.getBytes(StandardCharsets.UTF_8);
    }

    /*
     * Retorna a senha mestra a partir da variável de ambiente.
     * Em produção, esta variável deve ser configurada no ambiente
     * de execução (ex: systemd unit, Dockerfile, .env).
     */
    private static char[] obterSenhaMestra() {
        String env = System.getenv(ENV_KEY);
        if (env == null || env.isBlank()) {
            // Fallback para desenvolvimento local — NUNCA use em produção
            System.err.println("[AVISO DE SEGURANÇA] Variável " + ENV_KEY +
                    " não definida. Usando chave padrão de DESENVOLVIMENTO. " +
                    "CONFIGURE ESTA VARIÁVEL ANTES DE IR PARA PRODUÇÃO.");
            return "DEV_ONLY_INSECURE_KEY_CHANGE_ME".toCharArray();
        }
        return env.toCharArray();
    }

    /*
     * Heurística para detectar dados legados (texto puro não cifrado).
     * Um documento cifrado tem no mínimo salt+iv+tag = 44 bytes → 60+ chars Base64.
     */
    private static boolean isLegado(String valor) {
        try {
            byte[] decoded = Base64.getDecoder().decode(valor);
            return decoded.length < (SALT_BYTES + GCM_IV_BYTES + GCM_TAG_BITS / 8);
        } catch (IllegalArgumentException e) {
            return true; // Não é Base64 válido → dado legado
        }
    }

    // ── Exceção customizada ──────────────────────────────────────────────────
    public static class CryptoException extends RuntimeException {
        public CryptoException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
