package br.com.sosviale;

import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.service.TransferService;
import br.com.sosviale.view.LoginScreen;
import br.com.sosviale.view.MainDashboard;
import org.flywaydb.core.Flyway;

import javax.swing.*;

public class App {

    public static void main(String[] args) {
        //  Roda as migrações do banco de dados antes de abrir a UI
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

        // Inicia a Interface Gráfica
        SwingUtilities.invokeLater(() -> {
            try {
                AuthenticationService authService = new AuthenticationService();
                LoginScreen loginScreen = new LoginScreen(authService);

                loginScreen.setLoginCallback(username -> {
                    TransferService transferService = new TransferService();
                    MainDashboard dashboard = new MainDashboard(authService, transferService);
                    dashboard.setVisible(true);
                });

                loginScreen.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void configurarBancoDeDados() {

        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:postgresql://localhost:5600/sos_viale_db", "viale_user", "viale_password")
                .load();

        System.out.println("Verificando atualizações no banco de dados...");
        flyway.migrate();
        System.out.println("Flyway: Banco de dados pronto para uso!");
    }
}