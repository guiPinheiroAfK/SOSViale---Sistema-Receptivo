package br.com.sosviale.auth;

import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.util.JwtUtil;
import br.com.sosviale.util.JwtUtil.Claims;
import br.com.sosviale.util.JwtUtil.TokenInvalidoException;

import java.time.Instant;

/*
 * Gerenciador de sessão baseado em JWT para a aplicação desktop.
 *
 * PADRÃO SINGLETON: apenas uma sessão ativa por instância da JVM.
 *
 * FLUXO CORRETO:
 *   1. AuthenticationService.login() autentica → chama SessionManager.iniciarSessao(token)
 *   2. Qualquer parte da aplicação chama SessionManager.getInstance().getClaims()
 *      para saber quem está logado e qual perfil tem.
 *   3. No logout: SessionManager.getInstance().encerrarSessao()
 *
 * POR QUE ISSO IMPORTA vs o currentUser antigo?
 *   - O currentUser era apenas uma referência em memória sem expiração.
 *   - Com JWT, a sessão expira automaticamente após 8 horas.
 *   - O token pode ser validado criptograficamente — não é possível
 *     forjar uma sessão de admin apenas alterando um campo.
 */
public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String tokenAtual;      // JWT em memória (não persiste em disco)
    private Claims claimsAtual;     // Claims extraídas do token válido
    private boolean modoOffline;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  API PÚBLICA
    // ════════════════════════════════════════════════════════════════════════

    /*
     * Inicia a sessão com um token JWT recém-gerado após login bem-sucedido.
     *
     * @param token Token JWT gerado pelo AuthenticationService
     * @throws TokenInvalidoException se o token já estiver corrompido (não deve ocorrer
     *                                em fluxo normal, mas protege contra bugs)
     */
    public void iniciarSessao(String token) {
        this.claimsAtual = JwtUtil.validarToken(token); // valida antes de aceitar
        this.tokenAtual  = token;
        this.modoOffline = false;
    }

    /**
     * Sessão local sem JWT — permite consultar cache offline após primeira sincronização.
     */
    public void iniciarSessaoOffline(String usuario, String nome, Perfil perfil, boolean isAdmin) {
        long exp = (Instant.now().getEpochSecond() / 1000) + (365L * 24 * 3600);
        this.claimsAtual = new JwtUtil.Claims(usuario, nome, perfil, isAdmin, exp);
        this.tokenAtual = "OFFLINE_SESSION";
        this.modoOffline = true;
    }

    /*
     * Encerra a sessão atual (logout).
     * O token é descartado da memória. Não é necessário "revogar" no servidor
     * pois a aplicação é desktop/local — basta limpar o estado em memória.
     */
    public void encerrarSessao() {
        this.tokenAtual  = null;
        this.claimsAtual = null;
        this.modoOffline = false;
    }

    public boolean isModoOffline() {
        return modoOffline;
    }

    /**
     * Verifica se há uma sessão ativa e se o token ainda não expirou.
     */
    public boolean isAutenticado() {
        if (tokenAtual == null || claimsAtual == null) return false;
        if (modoOffline) return true;
        // Re-valida o token para checar expiração sem ir ao banco
        try {
            this.claimsAtual = JwtUtil.validarToken(tokenAtual);
            return true;
        } catch (TokenInvalidoException e) {
            encerrarSessao();
            return false;
        }
    }

    /*
     * Retorna o perfil do usuário logado ou lança exceção se não há sessão.
     */
    public Perfil getPerfilAtual() {
        assertAutenticado();
        return claimsAtual.perfil;
    }

    /*
     * Retorna as claims do token atual.
     */
    public Claims getClaims() {
        assertAutenticado();
        return claimsAtual;
    }

    /*
     * Retorna o nome de usuário da sessão ativa.
     */
    public String getUsuarioAtual() {
        assertAutenticado();
        return claimsAtual.usuario;
    }

    /*
     * Retorna o nome completo do usuário logado.
     */
    public String getNomeAtual() {
        assertAutenticado();
        return claimsAtual.nome;
    }

    /*
     * Atalho: verifica se o usuário logado é ADMIN.
     */
    public boolean isAdmin() {
        return isAutenticado() && claimsAtual.isAdmin;
    }

    /*
     * Verifica se o usuário tem pelo menos o perfil exigido.
     *
     * Hierarquia: ADMIN > GERENTE > MOTORISTA
     *
     * @param perfilMinimo Perfil mínimo necessário para a operação
     * @throws SessaoException se o usuário não tem permissão suficiente
     */
    public void exigirPerfil(Perfil perfilMinimo) {
        assertAutenticado();
        if (!temPerfil(perfilMinimo)) {
            throw new SessaoException("Acesso negado. Perfil necessário: "
                    + perfilMinimo + ". Seu perfil: " + claimsAtual.perfil);
        }
    }

    /*
     * Retorna o token JWT atual (para ser enviado em chamadas de API, se necessário).
     */
    public String getToken() {
        assertAutenticado();
        return tokenAtual;
    }

    /**
     * Retorna quantos segundos restam de sessão.
     */
    public long getSegundosRestantes() {
        if (!isAutenticado()) return 0;
        return claimsAtual.segundosRestantes();
    }

    // ════════════════════════════════════════════════════════════════════════
    //  MÉTODOS PRIVADOS
    // ════════════════════════════════════════════════════════════════════════

    private void assertAutenticado() {
        if (!isAutenticado()) {
            throw new SessaoException("Nenhuma sessão ativa. Faça login para continuar.");
        }
    }

    private boolean temPerfil(Perfil minimo) {
        // Hierarquia numérica: ADMIN=2, GERENTE=1, MOTORISTA=0
        return nivelPerfil(claimsAtual.perfil) >= nivelPerfil(minimo);
    }

    private int nivelPerfil(Perfil perfil) {
        return switch (perfil) {
            case ADMIN    -> 2;
            case GERENTE  -> 1;
            case MOTORISTA -> 0;
        };
    }

    // ── Exceção de sessão ────────────────────────────────────────────────────
    public static class SessaoException extends RuntimeException {
        public SessaoException(String message) { super(message); }
    }
}
