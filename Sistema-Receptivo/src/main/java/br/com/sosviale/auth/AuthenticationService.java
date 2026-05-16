package br.com.sosviale.auth;

import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.repository.UserRepository;
import br.com.sosviale.util.JwtUtil;
import br.com.sosviale.util.PasswordUtil;

/*
 * Serviço de autenticação — versão segura com BCrypt + JWT.
 *
 * MUDANÇAS EM RELAÇÃO À VERSÃO ANTERIOR:
 *
 *   ANTES (inseguro):
 *     user.getSenha().equals(senha)   ← comparação em texto puro
 *
 *   AGORA (seguro):
 *     PasswordUtil.verificarSenha(senha, user.getSenha())  ← BCrypt.checkpw()
 *
 *   Outras melhorias:
 *   - O login não expõe se o "usuário não existe" ou se a "senha está errada"
 *     separadamente — a mensagem genérica "Usuário ou senha incorretos" evita
 *     enumeração de usuários.
 *   - Após login bem-sucedido, gera um JWT e inicia a sessão no SessionManager.
 *   - Re-hash silencioso: se o usuário tem hash com work factor antigo (ou dado
 *     legado em texto puro da migration), a senha é re-hasheada no login.
 */
public class AuthenticationService {

    private final UserRepository userRepository = new UserRepository();

    // ════════════════════════════════════════════════════════════════════════
    //  LOGIN
    // ════════════════════════════════════════════════════════════════════════

    /*
     * Autentica o usuário e inicia uma sessão JWT.
     *
     * @param usuario Login do usuário
     * @param senha   Senha em texto puro digitada no formulário
     * @throws AuthenticationException se as credenciais forem inválidas
     */
    public void login(String usuario, String senha) throws AuthenticationException {
        // 1. Validação de entrada
        if (usuario == null || senha == null || usuario.trim().isEmpty()) {
            throw new AuthenticationException("Usuário e senha são obrigatórios.");
        }

        // 2. Buscar usuário no banco
        User user = userRepository.buscarPorUsuario(usuario.trim());

        // 3. Verificar senha com BCrypt
        //    IMPORTANTE: sempre executar verificarSenha() mesmo se user == null
        //    para evitar timing attacks por enumeração (a comparação leva ~250ms
        //    com BCrypt; retornar imediatamente ao não encontrar o usuário
        //    revelaria a diferença via tempo de resposta).
        String hashParaComparar = (user != null)
                ? user.getSenha()
                : "$2a$12$invalidhashusedfortimingprotectiononly000000000000000"; // dummy

        boolean senhaCorreta = PasswordUtil.verificarSenha(senha, hashParaComparar);

        if (user == null || !senhaCorreta) {
            // Mensagem genérica intencional — não revelar qual campo está errado
            throw new AuthenticationException("Usuário ou senha incorretos.");
        }

        // 4. Re-hash silencioso se necessário (migração de dados legados)
        if (PasswordUtil.precisaReHash(user.getSenha())) {
            String novoHash = PasswordUtil.hashSenha(senha);
            user.setSenha(novoHash);
            userRepository.atualizarSenha(user.getUsuario(), novoHash);
        }

        // 5. Gerar JWT e iniciar sessão
        String token = JwtUtil.gerarToken(user);
        SessionManager.getInstance().iniciarSessao(token);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  REGISTRO
    // ════════════════════════════════════════════════════════════════════════

    /*
     * Registra novo usuário. Requer confirmação com senha do administrador.
     *
     * @param nome       Nome completo
     * @param usuario    Login desejado
     * @param senha      Senha do novo usuário (mínimo 6 caracteres)
     * @param senhaAdmin Senha atual do administrador para autorizar o cadastro
     * @throws AuthenticationException se a senha do admin estiver errada
     * @throws ValidationException     se os dados forem inválidos
     */
    public void registrarUsuario(String nome, String usuario, String senha, String senhaAdmin)
            throws AuthenticationException, ValidationException {

        // 1. Verificar senha do admin com BCrypt
        User admin = userRepository.buscarAdmin();
        if (admin == null || !PasswordUtil.verificarSenha(senhaAdmin, admin.getSenha())) {
            throw new AuthenticationException("Senha do administrador incorreta.");
        }

        // 2. Validações de negócio
        validarDadosUsuario(usuario, senha);

        if (userRepository.buscarPorUsuario(usuario.trim()) != null) {
            throw new ValidationException("Nome de usuário já está em uso.");
        }

        // 3. Hash da senha antes de persistir
        String hashSenha = PasswordUtil.hashSenha(senha);

        userRepository.salvar(new User(nome.trim(), usuario.trim(), hashSenha, false));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  LOGOUT
    // ════════════════════════════════════════════════════════════════════════

    public void logout() {
        SessionManager.getInstance().encerrarSessao();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  CONSULTAS DE ESTADO (delegam ao SessionManager)
    // ════════════════════════════════════════════════════════════════════════

    public boolean isAuthenticated() {
        return SessionManager.getInstance().isAutenticado();
    }

    /*
     * @deprecated Use SessionManager.getInstance().getClaims() diretamente.
     *             Mantido para compatibilidade com código legado.
     */
    @Deprecated
    public User getCurrentUser() {
        if (!isAuthenticated()) return null;
        var claims = SessionManager.getInstance().getClaims();
        // Reconstrói um User parcial a partir das claims para compatibilidade
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

    // ════════════════════════════════════════════════════════════════════════
    //  PRIVADO
    // ════════════════════════════════════════════════════════════════════════

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