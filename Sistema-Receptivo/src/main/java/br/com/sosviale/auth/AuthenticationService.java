package br.com.sosviale.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Sistema de Autenticação com JWT
 * - Admin pré-cadastrado
 * - Registro de novos usuários requer senha do admin
 * - Tokens JWT para sessões
 */
public class AuthenticationService {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123"; // Mude em produção!
    private static final long TOKEN_EXPIRATION = 24 * 60 * 60 * 1000; // 24 horas
    private static final byte[] SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded();

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private String currentToken;
    private String currentUsername;

    public AuthenticationService() {
        // Cria admin padrão
        users.put(ADMIN_USERNAME, new User(ADMIN_USERNAME, ADMIN_PASSWORD, "Administrador", true));
    }

    /*
     * Realiza login do usuário
     * @return Token JWT se sucesso, null se falha
     */
    public String login(String username, String password) throws AuthenticationException {
        if (username == null || password == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Usuário e senha são obrigatórios");
        }

        User user = users.get(username);
        if (user == null || !user.password.equals(password)) {
            throw new AuthenticationException("Usuário ou senha incorretos");
        }

        // Gera token JWT
        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION))
                .claim("role", user.isAdmin ? "ADMIN" : "USER")
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        this.currentToken = token;
        this.currentUsername = username;

        return token;
    }

    /*
     * Registra novo usuário (requer senha do admin)
     */
    public void registerUser(String username, String password, String fullName, String adminPassword)
            throws AuthenticationException, ValidationException {

        // Valida senha do admin
        if (!ADMIN_PASSWORD.equals(adminPassword)) {
            throw new AuthenticationException("Senha do administrador incorreta");
        }

        // Validações
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Usuário não pode estar vazio");
        }

        if (password == null || password.length() < 6) {
            throw new ValidationException("Senha deve ter no mínimo 6 caracteres");
        }

        if (users.containsKey(username)) {
            throw new ValidationException("Usuário já existe");
        }

        // Cria novo usuário
        users.put(username, new User(username, password, fullName, false));
    }

    /*
     * Valida se há token válido
     */
    public boolean isAuthenticated() {
        return currentToken != null && currentUsername != null;
    }

    /*
     * Retorna usuário atualmente logado
     */
    public String getCurrentUser() {
        return currentUsername;
    }

    /*
     * Verifica se usuário é admin
     */
    public boolean isAdmin() {
        if (currentUsername == null) return false;
        User user = users.get(currentUsername);
        return user != null && user.isAdmin;
    }

    /*
     * Logout do usuário
     */
    public void logout() {
        currentToken = null;
        currentUsername = null;
    }

    /*
     * Retorna lista de todos os usuários (apenas para admin)
     */
    public List<User> listUsers() throws AuthenticationException {
        if (!isAdmin()) {
            throw new AuthenticationException("Apenas administrador pode listar usuários");
        }
        return new ArrayList<>(users.values());
    }

    /*
     * Deleta usuário (apenas para admin)
     */
    public void deleteUser(String username) throws AuthenticationException, ValidationException {
        if (!isAdmin()) {
            throw new AuthenticationException("Apenas administrador pode deletar usuários");
        }

        if (ADMIN_USERNAME.equals(username)) {
            throw new ValidationException("Não é possível deletar o administrador");
        }

        users.remove(username);
    }

    /*
     * Modelo de Usuário
     */
    public static class User {
        public final String username;
        public String password;
        public String fullName;
        public final boolean isAdmin;
        public final Date createdAt;

        public User(String username, String password, String fullName, boolean isAdmin) {
            this.username = username;
            this.password = password;
            this.fullName = fullName;
            this.isAdmin = isAdmin;
            this.createdAt = new Date();
        }

        @Override
        public String toString() {
            return fullName + " (" + (isAdmin ? "ADMIN" : "USER") + ")";
        }
    }
}

