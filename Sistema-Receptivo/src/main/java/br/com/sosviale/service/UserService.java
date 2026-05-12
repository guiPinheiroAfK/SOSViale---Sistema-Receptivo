package br.com.sosviale.service;

import br.com.sosviale.auth.AuthenticationException;
import br.com.sosviale.auth.ValidationException;
import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.repository.UserRepository;

import java.util.List;

public class UserService {

    private final UserRepository repository = new UserRepository();

    public void registrar(String nome, String usuario, String senha, String senhaAdmin, Perfil perfil)
            throws AuthenticationException, ValidationException {

        User admin = repository.buscarAdmin();
        if (admin == null || !admin.getSenha().equals(senhaAdmin))
            throw new AuthenticationException("Senha do administrador incorreta");

        if (usuario == null || usuario.trim().isEmpty())
            throw new ValidationException("Usuário não pode estar vazio");
        if (senha == null || senha.length() < 6)
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres");
        if (repository.buscarPorUsuario(usuario) != null)
            throw new ValidationException("Usuário já existe");

        User novoUser = new User(nome, usuario, senha, false);
        novoUser.setPerfil(perfil != null ? perfil : Perfil.ATENDENTE);
        repository.salvar(novoUser);
    }

    public void excluir(String usuario) throws AuthenticationException, ValidationException {
        if ("admin".equals(usuario))
            throw new ValidationException("Não é possível deletar o administrador");
        repository.excluir(usuario);
    }

    public List<User> listarTodos() {
        return repository.listarTodos();
    }

    public void verificarPermissao(User usuarioLogado, String modulo) throws ValidationException {
        if (usuarioLogado == null) {
            throw new ValidationException("Sessão inválida. Por favor, faça login novamente.");
        }

        // 1. Painel Inicial: Acesso livre para TODOS os perfis
        if ("dashboard".equalsIgnoreCase(modulo)) {
            return;
        }

        Perfil perfil = usuarioLogado.getPerfil();

        // 2. Administrador: Acesso global irrestrito
        if (perfil == Perfil.ADMIN) {
            return;
        }

        boolean bloqueado = false;

        // 3. Aplicação das suas regras exatas por módulo
        switch (modulo.toUpperCase()) {
            case "TRANSFERS":
            case "PASSAGEIROS":
            case "ORDENS":
                // Regra: Atendente e Gerente acessam. Motorista fica bloqueado.
                if (perfil == Perfil.MOTORISTA) {
                    bloqueado = true;
                }
                break;

            case "MOTORISTAS":
            case "VEICULOS":
                // Regra: Apenas Gerente (e Admin) acessam. Atendente e Motorista ficam bloqueados.
                if (perfil == Perfil.ATENDENTE || perfil == Perfil.MOTORISTA) {
                    bloqueado = true;
                }
                break;

            case "ADMIN":
            case "USUARIOS":
                // Regra: Apenas Admin acessa a gestão de usuários.
                if (perfil != Perfil.ADMIN) {
                    bloqueado = true;
                }
                break;

            default:
                // Segurança: Se for um módulo não mapeado, bloqueia por padrão.
                bloqueado = true;
        }

        if (bloqueado) {
            throw new ValidationException("Acesso negado. Você não tem permissão para acessar este módulo.");
        }
    }

}