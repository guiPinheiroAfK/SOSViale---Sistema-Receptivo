package br.com.sosviale;

import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.config.DatabaseConfig;
import br.com.sosviale.config.EnvLoader;
import br.com.sosviale.offline.ConnectivityService;
import br.com.sosviale.offline.OfflineStore;
import br.com.sosviale.service.TransferService;
import br.com.sosviale.view.LoginScreen;
import br.com.sosviale.view.MainDashboard;
import org.flywaydb.core.Flyway;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;

// ponto de entrada swing: tenta jdbc + flyway, senao modo offline se ja tiver snapshot
public class App {

    private static boolean databaseDisponivel;

    // --- ciclo de vida inicial ---

    public static void main(String[] args) {
        EnvLoader.load();
        databaseDisponivel = tentarConfigurarBanco();

        if (!databaseDisponivel && !OfflineStore.getInstance().hasAnySnapshot()) {
            JOptionPane.showMessageDialog(null,
                    "Não foi possível conectar ao banco e não há dados offline salvos.\n"
                            + "Conecte-se à rede na primeira utilização para sincronizar.",
                    "Erro de Inicialização",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        if (!databaseDisponivel) {
            JOptionPane.showMessageDialog(null,
                    "Sem conexão com o servidor. O sistema abrirá em modo offline\n"
                            + "usando os dados salvos no dispositivo.",
                    "Modo Offline",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                AuthenticationService authService = new AuthenticationService();
                LoginScreen loginScreen = new LoginScreen(authService, databaseDisponivel);

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

    // --- estado global leve pra login/dashboard ---

    public static boolean isDatabaseDisponivel() {
        return databaseDisponivel;
    }

    // --- jdbc + flyway na subida ---

    private static boolean tentarConfigurarBanco() {
        try {
            Class.forName("org.postgresql.Driver");
            try (Connection c = DriverManager.getConnection(
                    DatabaseConfig.JDBC_URL,
                    DatabaseConfig.JDBC_USER,
                    DatabaseConfig.JDBC_PASSWORD)) {
                if (!c.isValid(3)) return false;
            }

            Flyway flyway = Flyway.configure()
                    .dataSource(DatabaseConfig.JDBC_URL, DatabaseConfig.JDBC_USER, DatabaseConfig.JDBC_PASSWORD)
                    .load();
            System.out.println("Verificando atualizações no banco de dados...");
            flyway.migrate();
            System.out.println("Flyway: Banco de dados pronto para uso!");
            ConnectivityService.invalidateCache();
            return true;
        } catch (Exception e) {
            System.err.println("Banco indisponível: " + e.getMessage());
            ConnectivityService.invalidateCache();
            return false;
        }
    }
}
