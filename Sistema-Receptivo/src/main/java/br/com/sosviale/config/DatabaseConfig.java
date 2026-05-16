package br.com.sosviale.config;

// mesmos defaults do persistence pra jdbc direto no App (flyway + health check)

public final class DatabaseConfig {

    public static final String JDBC_URL = "jdbc:postgresql://localhost:5600/sos_viale_db";
    public static final String JDBC_USER = "viale_user";
    public static final String JDBC_PASSWORD = "viale_password";

    private DatabaseConfig() {}
}
