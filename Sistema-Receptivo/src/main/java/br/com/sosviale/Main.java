package br.com.sosviale;

import br.com.sosviale.config.DbConfig;
import br.com.sosviale.repository.ConnectionFactory;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {

        //O Flyway prepara o banco (Cria/Atualiza as tabelas)
        try {
            System.out.println("Iniciando migrações do Flyway...");
            DbConfig.setup();
            System.out.println("Flyway: Tabelas atualizadas!");
        } catch (Exception e) {
            System.err.println("Erro no Flyway: " + e.getMessage());
            // Se o Flyway falhar ele nao tenta o resto
            return;
        }

        //O teste de conexão do banco
        System.out.println("Tentando conectar ao banco SOS Viale na porta 5600...");

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn != null) {
                System.out.println("O Java finalmente conectou!");
            }
        } catch (Exception e) {
            System.err.println("Java não conseguiu chegar no banco.");
            System.err.println("Motivo: " + e.getMessage());
        }
    }
}