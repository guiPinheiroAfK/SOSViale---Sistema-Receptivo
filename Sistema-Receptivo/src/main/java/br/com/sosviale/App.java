package br.com.sosviale;

import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.view.LoginScreen;
import br.com.sosviale.view.ProtipoMainDashboard;
import org.flywaydb.core.Flyway;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        // Passo 1: Rodar as migrações do banco de dados antes de abrir a UI
        try {
            configurarBancoDeDados();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Erro ao conectar ao banco de dados: " + e.getMessage(),
                    "Erro de Inicialização",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            System.exit(1);
        }

        // Passo 2: Iniciar a Interface Gráfica
        SwingUtilities.invokeLater(() -> {
            try {
                AuthenticationService authService = new AuthenticationService();
                LoginScreen loginScreen = new LoginScreen(authService);

                loginScreen.setLoginCallback(username -> {
                    ProtipoMainDashboard dashboard = new ProtipoMainDashboard(authService);
                    dashboard.setVisible(true);
                });

                loginScreen.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void configurarBancoDeDados() {
        // Usando EXATAMENTE os dados do seu persistence.xml
        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:postgresql://localhost:5600/sos_viale_db", "viale_user", "viale_password")
                // Se seus arquivos estiverem em local diferente de src/main/resources/db/migration,
                // você precisaria adicionar .locations("caminho/aqui")
                .load();

        System.out.println("Verificando atualizações no banco de dados...");
        flyway.migrate();
        System.out.println("Flyway: Banco de dados pronto para uso!");
    }
}