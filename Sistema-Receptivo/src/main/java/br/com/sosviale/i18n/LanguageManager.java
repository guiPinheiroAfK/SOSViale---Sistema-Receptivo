package br.com.sosviale.i18n;

import java.util.*;

/*
 * LanguageManager - Gerenciador centralizado de idiomas
 * Suporta: Português (PT), Inglês (EN) e Espanhol (ES)
 */
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
    }

    private static LanguageManager instance;
    private Language currentLanguage;
    private final Map<Language, Map<String, String>> translations;
    private final List<LanguageChangeListener> listeners;

    private LanguageManager() {
        this.currentLanguage = Language.PORTUGUESE;
        this.translations = new HashMap<>();
        this.listeners = new ArrayList<>();
        loadTranslations();
    }

    public static synchronized LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    private void loadTranslations() {
        // Português
        Map<String, String> pt = new HashMap<>();
        pt.put("app.title", "SOS VIALE | Sistema Receptivo");
        pt.put("app.search.placeholder", "Buscar transfer, passageiro ou OS...");
        pt.put("button.logout", "Sair");
        pt.put("menu.modules", "MÓDULOS");
        pt.put("menu.admin", "ADMIN");
        pt.put("menu.dashboard", "📊 Painel Inicial");
        pt.put("menu.dashboard.subtitle", "Resumo operacional do dia");
        pt.put("menu.transfers", "🚗 Transfers");
        pt.put("menu.transfers.subtitle", "Cadastro e acompanhamento");
        pt.put("menu.passengers", "👥 Passageiros");
        pt.put("menu.passengers.subtitle", "Cadastro de passageiros");
        pt.put("menu.drivers", "🧑‍✈️ Motoristas");
        pt.put("menu.drivers.subtitle", "Gestão de motoristas");
        pt.put("menu.vehicles", "🚙 Veículos");
        pt.put("menu.vehicles.subtitle", "Controle da frota");
        pt.put("menu.orders", "📋 Ordens de Serviço");
        pt.put("menu.orders.subtitle", "Montagem de OS");
        pt.put("menu.users", "⚙️ Usuários");
        pt.put("menu.users.subtitle", "Gestão de usuários do sistema");
        pt.put("dialog.confirm.logout", "Tem certeza que deseja sair?");
        pt.put("dialog.confirm.title", "Confirmação");
        pt.put("dialog.info.title", "SOS VIALE");
        pt.put("language.pt", "Português");
        pt.put("language.en", "English");
        pt.put("language.es", "Español");
        pt.put("language.label", "Idioma");
        pt.put("version", "v2.0 Refatorado");
        pt.put("login.title", "SOS VIALE");
        pt.put("login.subtitle", "Sistema Receptivo Trinacional");
        pt.put("login.username.label", "Usuário:");
        pt.put("login.password.label", "Senha:");
        pt.put("login.button.login", "Entrar");
        pt.put("login.button.register", "Registrar");
        pt.put("login.link.register", "Não tem conta? Registre-se");
        pt.put("login.link.login", "Já tem conta? Faça login");
        pt.put("register.title", "Criar Conta");
        pt.put("register.username.label", "Usuário:");
        pt.put("register.email.label", "Email:");
        pt.put("register.password.label", "Senha:");
        pt.put("register.confirm.label", "Confirmar Senha:");
        pt.put("register.button.register", "Criar Conta");
        pt.put("register.button.cancel", "Cancelar");
        pt.put("error.invalid.credentials", "Usuário ou senha inválidos");
        pt.put("error.user.exists", "Usuário já existe");
        pt.put("error.passwords.mismatch", "As senhas não correspondem");
        pt.put("error.required.fields", "Todos os campos são obrigatórios");

        // Inglês
        Map<String, String> en = new HashMap<>();
        en.put("app.title", "SOS VIALE | Receptive System");
        en.put("app.search.placeholder", "Search transfer, passenger or OS...");
        en.put("button.logout", "Logout");
        en.put("menu.modules", "MODULES");
        en.put("menu.admin", "ADMIN");
        en.put("menu.dashboard", "📊 Dashboard");
        en.put("menu.dashboard.subtitle", "Daily operational summary");
        en.put("menu.transfers", "🚗 Transfers");
        en.put("menu.transfers.subtitle", "Registration and tracking");
        en.put("menu.passengers", "👥 Passengers");
        en.put("menu.passengers.subtitle", "Passenger registration");
        en.put("menu.drivers", "🧑‍✈️ Drivers");
        en.put("menu.drivers.subtitle", "Driver management");
        en.put("menu.vehicles", "🚙 Vehicles");
        en.put("menu.vehicles.subtitle", "Fleet control");
        en.put("menu.orders", "📋 Service Orders");
        en.put("menu.orders.subtitle", "Service order assembly");
        en.put("menu.users", "⚙️ Users");
        en.put("menu.users.subtitle", "System user management");
        en.put("dialog.confirm.logout", "Are you sure you want to logout?");
        en.put("dialog.confirm.title", "Confirmation");
        en.put("dialog.info.title", "SOS VIALE");
        en.put("language.pt", "Português");
        en.put("language.en", "English");
        en.put("language.es", "Español");
        en.put("language.label", "Language");
        en.put("version", "v2.0 Refactored");
        en.put("login.title", "SOS VIALE");
        en.put("login.subtitle", "Trinational Receptive System");
        en.put("login.username.label", "Username:");
        en.put("login.password.label", "Password:");
        en.put("login.button.login", "Login");
        en.put("login.button.register", "Register");
        en.put("login.link.register", "Don't have an account? Register");
        en.put("login.link.login", "Already have an account? Login");
        en.put("register.title", "Create Account");
        en.put("register.username.label", "Username:");
        en.put("register.email.label", "Email:");
        en.put("register.password.label", "Password:");
        en.put("register.confirm.label", "Confirm Password:");
        en.put("register.button.register", "Create Account");
        en.put("register.button.cancel", "Cancel");
        en.put("error.invalid.credentials", "Invalid username or password");
        en.put("error.user.exists", "User already exists");
        en.put("error.passwords.mismatch", "Passwords do not match");
        en.put("error.required.fields", "All fields are required");

        // Espanhol
        Map<String, String> es = new HashMap<>();
        es.put("app.title", "SOS VIALE | Sistema Receptivo");
        es.put("app.search.placeholder", "Buscar transferencia, pasajero u OS...");
        es.put("button.logout", "Cerrar sesión");
        es.put("menu.modules", "MÓDULOS");
        es.put("menu.admin", "ADMINISTRADOR");
        es.put("menu.dashboard", "📊 Panel Principal");
        es.put("menu.dashboard.subtitle", "Resumen operacional del día");
        es.put("menu.transfers", "🚗 Transferencias");
        es.put("menu.transfers.subtitle", "Registro y seguimiento");
        es.put("menu.passengers", "👥 Pasajeros");
        es.put("menu.passengers.subtitle", "Registro de pasajeros");
        es.put("menu.drivers", "🧑‍✈️ Conductores");
        es.put("menu.drivers.subtitle", "Gestión de conductores");
        es.put("menu.vehicles", "🚙 Vehículos");
        es.put("menu.vehicles.subtitle", "Control de flota");
        es.put("menu.orders", "📋 Órdenes de Servicio");
        es.put("menu.orders.subtitle", "Montaje de órdenes");
        es.put("menu.users", "⚙️ Usuarios");
        es.put("menu.users.subtitle", "Gestión de usuarios del sistema");
        es.put("dialog.confirm.logout", "¿Está seguro de que desea cerrar sesión?");
        es.put("dialog.confirm.title", "Confirmación");
        es.put("dialog.info.title", "SOS VIALE");
        es.put("language.pt", "Português");
        es.put("language.en", "English");
        es.put("language.es", "Español");
        es.put("language.label", "Idioma");
        es.put("version", "v2.0 Refactorizado");
        es.put("login.title", "SOS VIALE");
        es.put("login.subtitle", "Sistema Receptivo Trinacional");
        es.put("login.username.label", "Usuario:");
        es.put("login.password.label", "Contraseña:");
        es.put("login.button.login", "Iniciar Sesión");
        es.put("login.button.register", "Registrarse");
        es.put("login.link.register", "¿Sin cuenta? Regístrese");
        es.put("login.link.login", "¿Tiene cuenta? Inicie sesión");
        es.put("register.title", "Crear Cuenta");
        es.put("register.username.label", "Usuario:");
        es.put("register.email.label", "Correo Electrónico:");
        es.put("register.password.label", "Contraseña:");
        es.put("register.confirm.label", "Confirmar Contraseña:");
        es.put("register.button.register", "Crear Cuenta");
        es.put("register.button.cancel", "Cancelar");
        es.put("error.invalid.credentials", "Usuario o contraseña inválidos");
        es.put("error.user.exists", "El usuario ya existe");
        es.put("error.passwords.mismatch", "Las contraseñas no coinciden");
        es.put("error.required.fields", "Todos los campos son obligatorios");

        translations.put(Language.PORTUGUESE, pt);
        translations.put(Language.ENGLISH, en);
        translations.put(Language.SPANISH, es);
    }

    public String translate(String key) {
        Map<String, String> currentTranslations = translations.get(currentLanguage);
        return currentTranslations.getOrDefault(key, key);
    }

    public String translate(String key, String defaultValue) {
        Map<String, String> currentTranslations = translations.get(currentLanguage);
        return currentTranslations.getOrDefault(key, defaultValue);
    }

    public Language getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(Language language) {
        if (this.currentLanguage != language) {
            this.currentLanguage = language;
            notifyLanguageChanged();
        }
    }

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
