package br.com.sosviale;

import br.com.sosviale.repository.ConnectionFactory;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        System.out.println("Tentando conectar ao banco SOS Viale na porta 5600...");

        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn != null) {
                System.out.println("O Java finalmente conectou!");
            }
        } catch (Exception e) {
            System.err.println("Java não conseguiu chegar no banco.");
            System.err.println("Motivo: " + e.getMessage());
            // se der erro, o problema é: ou o Docker tá desligado ou a porta não é 5600
        }
    }
}