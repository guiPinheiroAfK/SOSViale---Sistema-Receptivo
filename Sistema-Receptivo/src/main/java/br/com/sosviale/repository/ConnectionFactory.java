package br.com.sosviale.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    // Dados diretos, sem frescura de arquivo externo agora
    private static final String URL = "jdbc:postgresql://localhost:5600/sos_viale_db";
    private static final String USER = "viale_user";
    private static final String PASS = "teste_viale";
    private static final String DRIVER = "org.postgresql.Driver";

    public static Connection getConnection() throws SQLException {
        try {
            // Carrega o driver que está no seu pom.xml
            Class.forName(DRIVER);
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver não encontrado! Verifique se o Maven baixou o Postgres no pom.xml");
        }
    }
}