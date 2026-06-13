package br.com.sosviale.i18n;

import br.com.sosviale.service.StatusTransfer;

import java.util.*;

// lê traduções dos .properties via ResourceBundle; telas só chamam translate / setLanguage

public class LanguageManager {

    public enum Language {
        PORTUGUESE("pt_BR"),
        ENGLISH("en_US"),
        SPANISH("es_ES");

        private final String code;

        Language(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public Locale toLocale() {
            String[] parts = code.split("_");
            return parts.length == 2
                    ? new Locale(parts[0], parts[1])
                    : new Locale(parts[0]);
        }
    }

    private static final String BUNDLE_BASE = "messages";

    private static LanguageManager instance;
    private Language currentLanguage;
    private ResourceBundle bundle;
    private final List<LanguageChangeListener> listeners;

    private LanguageManager() {
        this.currentLanguage = Language.PORTUGUESE;
        this.listeners = new ArrayList<>();
        this.bundle = loadBundle(currentLanguage);
    }

    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    private ResourceBundle loadBundle(Language lang) {
        try {
            return ResourceBundle.getBundle(
                    BUNDLE_BASE,
                    lang.toLocale(),
                    ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES)
            );
        } catch (MissingResourceException e) {
            // fallback para pt_BR se o arquivo do idioma não for encontrado
            return ResourceBundle.getBundle(BUNDLE_BASE, Language.PORTUGUESE.toLocale());
        }
    }

    // --- lookup direto ---

    public String translate(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public String translate(String key, String defaultValue) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return defaultValue;
        }
    }

    public String translate(String key, Map<String, String> params) {
        String text = translate(key);
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                text = text.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return text;
    }

    public String translateStatus(StatusTransfer status) {
        if (status == null) return "";
        return translate("status.transfer." + status.name());
    }

    // --- lingua atual ---

    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(Language language) {
        if (this.currentLanguage != language) {
            this.currentLanguage = language;
            this.bundle = loadBundle(language);
            notifyLanguageChanged();
        }
    }

    // --- observers ---

    public void addLanguageChangeListener(LanguageChangeListener listener) {
        listeners.add(listener);
    }

    public void removeLanguageChangeListener(LanguageChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyLanguageChanged() {
        for (LanguageChangeListener listener : listeners) {
            listener.onLanguageChanged(currentLanguage);
        }
    }

    public interface LanguageChangeListener {
        void onLanguageChanged(Language newLanguage);
    }
}
