package br.com.sosviale.auth;

import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.repository.UserRepository;

public class AuthenticationService {

    private final UserRepository userRepository = new UserRepository();
    private User currentUser;

    public void login(String usuario, String senha) throws AuthenticationException {
        if (usuario == null || senha == null || usuario.trim().isEmpty()) {
            throw new AuthenticationException("Usuário e senha são obrigatórios");
        }

        User user = userRepository.buscarPorUsuario(usuario);

        if (user == null || !user.getSenha().equals(senha)) {
            throw new AuthenticationException("Usuário ou senha incorretos");
        }

        this.currentUser = user;
    }

    public void registrarUsuario(String nome, String usuario, String senha, String senhaAdmin, Perfil perfil)
            throws AuthenticationException, ValidationException {

        User admin = userRepository.buscarAdmin();
        if (admin == null || !admin.getSenha().equals(senhaAdmin)) {
            throw new AuthenticationException("Senha do administrador incorreta");
        }

        if (usuario == null || usuario.trim().isEmpty()) {
            throw new ValidationException("Usuário não pode estar vazio");
        }
        if (senha == null || senha.length() < 6) {
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres");
        }
        if (userRepository.buscarPorUsuario(usuario) != null) {
            throw new ValidationException("Usuário já existe");
        }

        User novoUser = new User(nome, usuario, senha, false);
        novoUser.setPerfil(perfil != null ? perfil : Perfil.ATENDENTE);
        userRepository.salvar(novoUser);
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public Perfil getPerfil() {
        return currentUser != null ? currentUser.getPerfil() : null;
    }

    public void logout() {
        this.currentUser = null;
    }
}