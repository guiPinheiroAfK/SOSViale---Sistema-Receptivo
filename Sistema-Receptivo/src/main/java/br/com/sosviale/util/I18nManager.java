package br.com.sosviale.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;


//gerenciador centralizado de internacionalização (i18n)
//suporta: Português (PT), English (EN), Español (ES)

public class I18nManager {

    private static final String BUNDLE_NAME = "messages";
    private static ResourceBundle bundle;
    private static Locale currentLocale = new Locale("pt", "BR");

    // idiomas suportados
    private static final Map<String, Locale> SUPPORTED_LOCALES = new HashMap<>();
    static {
        SUPPORTED_LOCALES.put("pt", new Locale("pt", "BR"));
        SUPPORTED_LOCALES.put("en", new Locale("en", "US"));
        SUPPORTED_LOCALES.put("es", new Locale("es", "ES"));
    }

    static {
        // inicializa com Português por padrão
        init("pt");
    }

    // inicializa com um idioma específico
    public static void init(String languageCode) {
        currentLocale = SUPPORTED_LOCALES.getOrDefault(languageCode, new Locale("pt", "BR"));
        try {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
        } catch (Exception e) {
            System.err.println("Erro ao carregar bundle: " + e.getMessage());
            bundle = ResourceBundle.getBundle(BUNDLE_NAME, new Locale("pt", "BR"));
        }
    }

    // obtem mensagem traduzida
    public static String get(String key) {
        if (bundle == null) {
            init("pt");
        }
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key; // Retorna a chave se não encontrar
        }
    }

    // obtem mensagem com parâmetros

    public static String get(String key, Object... args) {
        String message = get(key);
        try {
            return String.format(message, args);
        } catch (Exception e) {
            return message;
        }
    }

    // retorna idioma atual
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    // lista idiomas suportados

    public static String[] getSupportedLanguages() {
        return new String[]{"pt", "en", "es"};
    }

    // retorna nome amigavel do idioma

    public static String getLanguageName(String code) {
        switch (code) {
            case "pt": return "Português";
            case "en": return "English";
            case "es": return "Español";
            default: return code;
        }
    }
}
