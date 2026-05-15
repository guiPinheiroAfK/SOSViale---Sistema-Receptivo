package br.com.sosviale.util;

import br.com.sosviale.config.SecretsConfig;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

/*
 * Implementação leve de JWT (JSON Web Token) usando HMAC-SHA256.
 *
 * POR QUE JWT?
 *   - Stateless: o servidor não precisa manter tabela de sessões em memória
 *     ou no banco. O token carrega tudo (perfil, expiração, id do usuário).
 *   - Verificável: qualquer parte do sistema pode validar o token com a
 *     chave secreta, sem ir ao banco.
 *   - Seguro contra falsificação: a assinatura HMAC-SHA256 garante que
 *     ninguém pode alterar o payload sem invalidar a assinatura.
 *
 * ESTRUTURA DO TOKEN:
 *   Base64Url(header) + "." + Base64Url(payload) + "." + Base64Url(assinatura)
 *
 * PAYLOAD gerado:
 *   { "sub": "usuario", "perfil": "ADMIN", "isAdmin": true,
 *     "iat": 1234567890, "exp": 1234571490 }
 *
 * IMPORTANTE: Esta implementação usa apenas a biblioteca padrão do Java (javax.crypto).
 * Para projetos maiores, considere a lib io.jsonwebtoken:jjwt.
 */
public final class JwtUtil {

    // ── Configuração ──────────────────────────────────────────────────────────
    private static final String HMAC_ALGO     = "HmacSHA256";
    private static final long   EXPIRACAO_MS  = 8 * 60 * 60 * 1000L; // 8 horas
    // Header JWT fixo para HMAC-SHA256
    private static final String HEADER_B64 = base64url(
            "{\"alg\":\"HS256\",\"typ\":\"JWT\"}");

    private JwtUtil() {}

    // ════════════════════════════════════════════════════════════════════════
    //  API PÚBLICA
    // ════════════════════════════════════════════════════════════════════════

    /*
     * Gera um token JWT para o usuário autenticado.
     *
     * @param user Usuário que acabou de fazer login com sucesso
     * @return Token JWT assinado, válido por EXPIRACAO_MS milissegundos
     */
    public static String gerarToken(User user) {
        long agora = Instant.now().toEpochMilli() / 1000;
        long exp   = agora + (EXPIRACAO_MS / 1000);

        // Payload JSON construído manualmente para evitar dependência extra
        String payload = "{"
                + "\"sub\":\"" + escapar(user.getUsuario()) + "\","
                + "\"nome\":\"" + escapar(user.getNome()) + "\","
                + "\"perfil\":\"" + user.getPerfil().name() + "\","
                + "\"isAdmin\":" + user.isAdmin() + ","
                + "\"iat\":" + agora + ","
                + "\"exp\":" + exp
                + "}";

        String payloadB64   = base64url(payload);
        String cabecalho    = HEADER_B64 + "." + payloadB64;
        String assinatura   = assinar(cabecalho);

        return cabecalho + "." + assinatura;
    }

    /*
     * Valida um token JWT e retorna suas claims se válido.
     *
     * @param token Token JWT recebido
     * @return Claims extraídas do token
     * @throws TokenInvalidoException se o token for inválido, expirado ou falsificado
     */
    public static Claims validarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenInvalidoException("Token não fornecido.");
        }

        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new TokenInvalidoException("Formato de token inválido.");
        }

        // 1. Verificar assinatura (timing-safe)
        String cabecalho   = partes[0] + "." + partes[1];
        String assinaturaEsperada = assinar(cabecalho);
        if (!comparacaoSegura(assinaturaEsperada, partes[2])) {
            throw new TokenInvalidoException("Assinatura do token inválida. Possível adulteração.");
        }

        // 2. Decodificar payload
        String payloadJson = new String(
                Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);

        // 3. Verificar expiração
        long exp = extrairLong(payloadJson, "exp");
        if (Instant.now().getEpochSecond() > exp) {
            throw new TokenInvalidoException("Token expirado. Faça login novamente.");
        }

        // 4. Extrair e retornar claims
        return new Claims(
                extrairString(payloadJson, "sub"),
                extrairString(payloadJson, "nome"),
                Perfil.valueOf(extrairString(payloadJson, "perfil")),
                extrairBoolean(payloadJson, "isAdmin"),
                exp
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    //  RECORD DE CLAIMS
    // ════════════════════════════════════════════════════════════════════════

    /** Dados extraídos de um token JWT válido. */
    public static class Claims {
        public final String  usuario;
        public final String  nome;
        public final Perfil  perfil;
        public final boolean isAdmin;
        public final long    expEpochSeconds;

        public Claims(String usuario, String nome, Perfil perfil,
                      boolean isAdmin, long expEpochSeconds) {
            this.usuario         = usuario;
            this.nome            = nome;
            this.perfil          = perfil;
            this.isAdmin         = isAdmin;
            this.expEpochSeconds = expEpochSeconds;
        }

        public long segundosRestantes() {
            return expEpochSeconds - Instant.now().getEpochSecond();
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MÉTODOS PRIVADOS
    // ════════════════════════════════════════════════════════════════════════

    private static String assinar(String dados) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(obterChaveSecreta(), HMAC_ALGO));
            byte[] hash = mac.doFinal(dados.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao assinar token JWT.", e);
        }
    }

    /** Comparação em tempo constante para evitar timing attacks. */
    private static boolean comparacaoSegura(String a, String b) {
        if (a == null || b == null) return false;
        byte[] ba = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (ba.length != bb.length) return false;
        int diff = 0;
        for (int i = 0; i < ba.length; i++) {
            diff |= ba[i] ^ bb[i];
        }
        return diff == 0;
    }

    private static byte[] obterChaveSecreta() {
        return SecretsConfig.jwtSecretBytes();
    }

    private static String base64url(String texto) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(texto.getBytes(StandardCharsets.UTF_8));
    }

    private static String escapar(String s) {
        return s == null ? "" : s.replace("\"", "\\\"");
    }

    // ── Extratores simples de JSON (evita dependência de lib JSON) ──────────
    private static String extrairString(String json, String chave) {
        String pattern = "\"" + chave + "\":\"";
        int inicio = json.indexOf(pattern);
        if (inicio < 0) return "";
        inicio += pattern.length();
        int fim = json.indexOf("\"", inicio);
        return fim < 0 ? "" : json.substring(inicio, fim);
    }

    private static long extrairLong(String json, String chave) {
        String pattern = "\"" + chave + "\":";
        int inicio = json.indexOf(pattern);
        if (inicio < 0) return 0;
        inicio += pattern.length();
        int fim = inicio;
        while (fim < json.length() && (Character.isDigit(json.charAt(fim)) || json.charAt(fim) == '-')) fim++;
        try { return Long.parseLong(json.substring(inicio, fim)); }
        catch (NumberFormatException e) { return 0; }
    }

    private static boolean extrairBoolean(String json, String chave) {
        String pattern = "\"" + chave + "\":";
        int inicio = json.indexOf(pattern);
        if (inicio < 0) return false;
        return json.startsWith("true", inicio + pattern.length());
    }

    // ── Exceção ──────────────────────────────────────────────────────────────
    public static class TokenInvalidoException extends RuntimeException {
        public TokenInvalidoException(String message) { super(message); }
    }
}
