package br.com.sosviale.config;

/**
 * Configuração JDBC centralizada (mesmos valores do persistence.xml).
 */
public final class DatabaseConfig {

    public static final String JDBC_URL = "jdbc:postgresql://localhost:5600/sos_viale_db";
    public static final String JDBC_USER = "viale_user";
    public static final String JDBC_PASSWORD = "viale_password";

    private DatabaseConfig() {}
}
