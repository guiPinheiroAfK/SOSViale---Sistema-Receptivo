package br.com.sosviale.service;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.repository.UserRepository;
import br.com.sosviale.util.PasswordUtil;

import java.util.List;

public class UserService {

    private final UserRepository repository = new UserRepository();

    //cadastro
    public void registrar(String nome, String usuario, String senha,
                          String senhaAdmin, Perfil perfil)
            throws AuthenticationException, ValidationException {

        //verifica autorização do admin com BCrypt
        User admin = repository.buscarAdmin();
        if (admin == null || !PasswordUtil.verificarSenha(senhaAdmin, admin.getSenha())) {
            throw new AuthenticationException("Senha do administrador incorreta.");
        }

        // validações
        if (usuario == null || usuario.trim().isEmpty())
            throw new ValidationException("Usuário não pode estar vazio.");
        if (senha == null || senha.length() < 6)
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres.");
        if (senha.length() > 128)
            throw new ValidationException("Senha muito longa (máximo 128 caracteres).");
        if (repository.buscarPorUsuario(usuario.trim()) != null)
            throw new ValidationException("Nome de usuário já está em uso.");

        // hash da senha — NUNCA persistir texto puro
        String hashSenha = PasswordUtil.hashSenha(senha);

        User novoUser = new User(nome.trim(), usuario.trim(), hashSenha, false);
        novoUser.setPerfil(perfil != null ? perfil : Perfil.GERENTE);
        repository.salvar(novoUser);
    }


    //  alteracao de senha
    public void alterarSenha(String usuario, String senhaAtual, String novaSenha)
            throws AuthenticationException, ValidationException {

        User user = repository.buscarPorUsuario(usuario);
        if (user == null) {
            throw new ValidationException("Usuário não encontrado.");
        }

        // Verificar senha atual com BCrypt
        if (!PasswordUtil.verificarSenha(senhaAtual, user.getSenha())) {
            throw new AuthenticationException("Senha atual incorreta.");
        }

        if (novaSenha == null || novaSenha.length() < 6) {
            throw new ValidationException("Nova senha deve ter no mínimo 6 caracteres.");
        }
        if (novaSenha.length() > 128) {
            throw new ValidationException("Senha muito longa (máximo 128 caracteres).");
        }

        String novoHash = PasswordUtil.hashSenha(novaSenha);
        repository.atualizarSenha(usuario, novoHash);
    }

    public void resetarSenhaAdmin(String usuarioAlvo, String novaSenha, String senhaAdmin)
            throws AuthenticationException, ValidationException {

        User admin = repository.buscarAdmin();
        if (admin == null || !PasswordUtil.verificarSenha(senhaAdmin, admin.getSenha())) {
            throw new AuthenticationException("Senha do administrador incorreta.");
        }

        if (novaSenha == null || novaSenha.length() < 6) {
            throw new ValidationException("Nova senha deve ter no mínimo 6 caracteres.");
        }

        User alvo = repository.buscarPorUsuario(usuarioAlvo);
        if (alvo == null) {
            throw new ValidationException("Usuário não encontrado: " + usuarioAlvo);
        }

        repository.atualizarSenha(usuarioAlvo, PasswordUtil.hashSenha(novaSenha));
    }

    public void excluir(String usuario)
            throws AuthenticationException, ValidationException {

        if ("admin".equalsIgnoreCase(usuario)) {
            throw new ValidationException("Não é possível excluir o administrador do sistema.");
        }

        repository.excluir(usuario);
    }

    // atualiza nome e perfil, usuário de login não é alterad

    public void atualizar(String usuario, String nome, Perfil perfil, String senhaAdmin)
            throws AuthenticationException, ValidationException {
        User admin = repository.buscarAdmin();
        if (admin == null || !PasswordUtil.verificarSenha(senhaAdmin, admin.getSenha())) {
            throw new AuthenticationException("Senha do administrador incorreta.");
        }
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException("Nome é obrigatório.");
        }

        User alvo = repository.buscarPorUsuario(usuario);
        if (alvo == null) {
            throw new ValidationException("Usuário não encontrado.");
        }

        alvo.setNome(nome.trim());
        if (alvo.isAdmin()) {
            alvo.setPerfil(Perfil.ADMIN);
        } else {
            alvo.setPerfil(perfil != null ? perfil : Perfil.GERENTE);
        }
        repository.atualizar(alvo);
    }

    // consultas

    public List<User> listarTodos() {
        return repository.listarTodos();
    }

    public void verificarPermissao(User usuarioLogado, String modulo) throws ValidationException {
        if (usuarioLogado == null) {
            throw new ValidationException("Sessão inválida. Por favor, faça login novamente.");
        }

        if ("dashboard".equalsIgnoreCase(modulo)) return;

        Perfil perfil = usuarioLogado.getPerfil();
        if (perfil == Perfil.ADMIN) return;

        boolean bloqueado = switch (modulo.toUpperCase()) {
            case "TRANSFERS", "PASSAGEIROS", "ORDENS" ->
                    perfil == Perfil.MOTORISTA;
            case "MOTORISTAS", "VEICULOS" ->
                    perfil == Perfil.MOTORISTA;
            case "ADMIN", "USUARIOS" ->
                    perfil != Perfil.ADMIN;
            default -> true; // Segurança: módulo desconhecido = bloqueado
        };

        if (bloqueado) {
            throw new ValidationException(
                    "Acesso negado. Você não tem permissão para acessar o módulo: " + modulo);
        }
    }
}
