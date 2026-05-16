package br.com.sosviale.auth;

import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.offline.OfflineSyncService;
import br.com.sosviale.offline.ConnectivityService;
import br.com.sosviale.repository.UserRepository;
import br.com.sosviale.util.JwtUtil;
import br.com.sosviale.util.PasswordUtil;

// bcrypt + jwt; login nunca diz se usuario existe ou só senha ruim

public class AuthenticationService {

    private final UserRepository userRepository = new UserRepository();

    // --- login ---

    public void login(String usuario, String senha) throws AuthenticationException {
        if (usuario == null || senha == null || usuario.trim().isEmpty()) {
            throw new AuthenticationException("Usuário e senha são obrigatórios.");
        }

        User user = userRepository.buscarPorUsuario(usuario.trim());

        String hashParaComparar = (user != null)
                ? user.getSenha()
                : "$2a$12$invalidhashusedfortimingprotectiononly000000000000000";

        boolean senhaCorreta = PasswordUtil.verificarSenha(senha, hashParaComparar);

        if (user == null || !senhaCorreta) {
            throw new AuthenticationException("Usuário ou senha incorretos.");
        }

        if (PasswordUtil.precisaReHash(user.getSenha())) {
            String novoHash = PasswordUtil.hashSenha(senha);
            user.setSenha(novoHash);
            userRepository.atualizarSenha(user.getUsuario(), novoHash);
        }

        String token = JwtUtil.gerarToken(user);
        SessionManager.getInstance().iniciarSessao(token);

        OfflineSyncService offlineSync = new OfflineSyncService();
        offlineSync.saveSessionAfterLogin(user);
        if (ConnectivityService.isDatabaseOnline()) {
            offlineSync.syncFromServer(user.getUsuario());
        }
    }

    // --- registro sob senha admin ---

    public void registrarUsuario(String nome, String usuario, String senha, String senhaAdmin)
            throws AuthenticationException, ValidationException {

        User admin = userRepository.buscarAdmin();
        if (admin == null || !PasswordUtil.verificarSenha(senhaAdmin, admin.getSenha())) {
            throw new AuthenticationException("Senha do administrador incorreta.");
        }

        validarDadosUsuario(usuario, senha);

        if (userRepository.buscarPorUsuario(usuario.trim()) != null) {
            throw new ValidationException("Nome de usuário já está em uso.");
        }

        String hashSenha = PasswordUtil.hashSenha(senha);

        userRepository.salvar(new User(nome.trim(), usuario.trim(), hashSenha, false));
    }

    // --- logout ---

    public void logout() {
        SessionManager.getInstance().encerrarSessao();
    }

    // --- estado (delegado pro singleton session) ---

    public boolean isAuthenticated() {
        return SessionManager.getInstance().isAutenticado();
    }

    @Deprecated // preferir SessionManager#getClaims onde der
    public User getCurrentUser() {
        if (!isAuthenticated()) return null;
        var claims = SessionManager.getInstance().getClaims();
        User u = new User();
        u.setUsuario(claims.usuario);
        u.setNome(claims.nome);
        u.setPerfil(claims.perfil);
        u.setAdmin(claims.isAdmin);
        return u;
    }

    public boolean isAdmin() {
        return SessionManager.getInstance().isAdmin();
    }

    // --- helpers ---

    private void validarDadosUsuario(String usuario, String senha) throws ValidationException {
        if (usuario == null || usuario.trim().isEmpty()) {
            throw new ValidationException("Usuário não pode estar vazio.");
        }
        if (usuario.trim().length() < 3) {
            throw new ValidationException("Usuário deve ter no mínimo 3 caracteres.");
        }
        if (senha == null || senha.length() < 6) {
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres.");
        }
        if (senha.length() > 128) {
            throw new ValidationException("Senha não pode ter mais de 128 caracteres.");
        }
    }
}
