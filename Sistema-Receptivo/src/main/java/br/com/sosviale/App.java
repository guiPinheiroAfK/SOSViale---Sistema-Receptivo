package br.com.sosviale;

import br.com.sosviale.auth.AuthenticationService;
import br.com.sosviale.view.LoginScreen;
import br.com.sosviale.view.MainDashboard;

import javax.swing.*;

/*
 * Ponto de entrada da aplicação SOS VIALE
 *
 * INSTRUÇÕES:
 * 1. Copie os arquivos em seu projeto:
 *    - AuthenticationService.java → src/main/java/br/com/sosviale/auth/
 *    - LoginScreen.java → src/main/java/br/com/sosviale/gui/
 *    - MainDashboard_Novo.java → renomear para MainDashboard.java em src/main/java/br/com/sosviale/gui/
 *    - App.java → src/main/java/br/com/sosviale/
 *
 * 2. Execute: java -cp . br.com.sosviale.App
 *    OU rode direto da IDE
 *
 * 3. LOGIN TESTE:
 *    - Usuário: admin
 *    - Senha: admin123
 *
 * 4. Para criar novo usuário:
 *    - Clique em "Criar conta"
 *    - Preencha dados
 *    - Senha do Admin: admin123
 */
public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Cria serviço de autenticação
                AuthenticationService authService = new AuthenticationService();

                // Cria tela de login
                LoginScreen loginScreen = new LoginScreen(authService);

                // Define callback para quando login é bem-sucedido
                loginScreen.setLoginCallback(username -> {
                    // Abre MainDashboard
                    MainDashboard dashboard = new MainDashboard(authService);
                    dashboard.setVisible(true);
                });

                loginScreen.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        null,
                        "Erro ao inicializar aplicação: " + e.getMessage(),
                        "Erro Fatal",
                        JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
