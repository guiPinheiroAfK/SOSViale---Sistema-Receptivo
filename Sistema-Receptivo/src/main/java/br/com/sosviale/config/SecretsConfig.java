package br.com.sosviale.config;

import java.nio.charset.StandardCharsets;

// jwt + crypto key vindo do env; dev cai nos defaults gritando uma vez no stderr

public final class SecretsConfig {

    public static final String ENV_JWT_SECRET = "RECEPTIVO_JWT_SECRET";
    public static final String ENV_CRYPTO_KEY = "RECEPTIVO_CRYPTO_KEY";

    private static final String DEFAULT_JWT =
            "DEV_JWT_SECRET_CHANGE_IN_PRODUCTION_MIN_32_CHARS!!";
    private static final String DEFAULT_CRYPTO = "DEV_ONLY_INSECURE_KEY_CHANGE_ME";

    private static byte[] jwtSecretBytes;
    private static char[] cryptoKeyChars;
    private static boolean jwtWarned;
    private static boolean cryptoWarned;

    private SecretsConfig() {}

    public static byte[] jwtSecretBytes() {
        if (jwtSecretBytes == null) {
            synchronized (SecretsConfig.class) {
                if (jwtSecretBytes == null) {
                    jwtSecretBytes = resolve(ENV_JWT_SECRET, DEFAULT_JWT, true).getBytes(StandardCharsets.UTF_8);
                }
            }
        }
        return jwtSecretBytes;
    }

    public static char[] cryptoKeyChars() {
        if (cryptoKeyChars == null) {
            synchronized (SecretsConfig.class) {
                if (cryptoKeyChars == null) {
                    cryptoKeyChars = resolve(ENV_CRYPTO_KEY, DEFAULT_CRYPTO, false).toCharArray();
                }
            }
        }
        return cryptoKeyChars;
    }

    // acha valor ou volta pro default de dev (com warn unico por tipo)

    private static String resolve(String envKey, String devDefault, boolean jwt) {
        EnvLoader.load();
        String value = EnvLoader.get(envKey);
        if (value != null && !value.isBlank()) {
            return value;
        }
        if (jwt) {
            if (!jwtWarned) {
                warnMissing(envKey);
                jwtWarned = true;
            }
        } else {
            if (!cryptoWarned) {
                warnMissing(envKey);
                cryptoWarned = true;
            }
        }
        return devDefault;
    }

    private static void warnMissing(String envKey) {
        System.err.println("[AVISO DE SEGURANÇA] Variável " + envKey
                + " não definida. Usando valor padrão de DESENVOLVIMENTO. "
                + "Defina em .env (copie de .env.example) ou nas variáveis de ambiente "
                + "antes de ir para produção.");
    }
}
