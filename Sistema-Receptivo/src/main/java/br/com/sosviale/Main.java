package br.com.sosviale;

import br.com.sosviale.config.DbConfig;
import br.com.sosviale.repository.ConnectionFactory;
import br.com.sosviale.repository.PassageiroRepository;
import java.sql.Connection;
import br.com.sosviale.model.Passageiro;

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

        try {
            PassageiroRepository repo = new PassageiroRepository();

            // Criando um objeto para teste
            Passageiro p = new Passageiro("Guilherme Gocks", "123.456.789-00", "Brasileira");

            System.out.println("Salvando passageiro via JPA...");
            repo.salvar(p);

            System.out.println("Passageiro salvo com ID: " + p.getId());

        } catch (Exception e) {
            System.err.println("Erro ao operar com JPA: " + e.getMessage());
            e.printStackTrace();
        }
    }
    }