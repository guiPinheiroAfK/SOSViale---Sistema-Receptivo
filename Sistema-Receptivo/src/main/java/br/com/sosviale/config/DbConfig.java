package br.com.sosviale.config;

import org.flywaydb.core.Flyway;

public class DbConfig {
    public static void setup() {
        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:postgresql://localhost:5600/sos_viale_db", "viale_user", "viale_password")
                .load();
        flyway.migrate();
    }
}
