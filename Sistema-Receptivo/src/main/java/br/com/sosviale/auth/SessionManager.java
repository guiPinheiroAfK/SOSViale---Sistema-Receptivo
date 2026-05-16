package br.com.sosviale.auth;

import br.com.sosviale.model.Perfil;
import br.com.sosviale.model.User;
import br.com.sosviale.util.JwtUtil;
import br.com.sosviale.util.JwtUtil.Claims;
import br.com.sosviale.util.JwtUtil.TokenInvalidoException;

import java.time.Instant;

// singleton: jwt em memoria, ou sessao marcada offline sem ir no servidor

public final class SessionManager {

    private static final SessionManager INSTANCE = new SessionManager();

    private String tokenAtual;
    private Claims claimsAtual;
    private boolean modoOffline;

    private SessionManager() {}

    // --- singleton ---

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    // --- inicio sessao ---

    public void iniciarSessao(String token) {
        this.claimsAtual = JwtUtil.validarToken(token);
        this.tokenAtual = token;
        this.modoOffline = false;
    }

    // cache local pos sync: token fake, mesmo shape de dados que o jwt
    public void iniciarSessaoOffline(String usuario, String nome, Perfil perfil, boolean isAdmin) {
        long exp = (Instant.now().getEpochSecond() / 1000) + (365L * 24 * 3600);
        this.claimsAtual = new JwtUtil.Claims(usuario, nome, perfil, isAdmin, exp);
        this.tokenAtual = "OFFLINE_SESSION";
        this.modoOffline = true;
    }

    public void encerrarSessao() {
        this.tokenAtual = null;
        this.claimsAtual = null;
        this.modoOffline = false;
    }

    public boolean isModoOffline() {
        return modoOffline;
    }

    // --- autenticado? ---

    public boolean isAutenticado() {
        if (tokenAtual == null || claimsAtual == null) return false;
        if (modoOffline) return true;
        try {
            this.claimsAtual = JwtUtil.validarToken(tokenAtual);
            return true;
        } catch (TokenInvalidoException e) {
            encerrarSessao();
            return false;
        }
    }

    // --- leituras da sessao ---

    public Perfil getPerfilAtual() {
        assertAutenticado();
        return claimsAtual.perfil;
    }

    public Claims getClaims() {
        assertAutenticado();
        return claimsAtual;
    }

    public String getUsuarioAtual() {
        assertAutenticado();
        return claimsAtual.usuario;
    }

    public String getNomeAtual() {
        assertAutenticado();
        return claimsAtual.nome;
    }

    public boolean isAdmin() {
        return isAutenticado() && claimsAtual.isAdmin;
    }

    // admin > gerente > motorista
    public void exigirPerfil(Perfil perfilMinimo) {
        assertAutenticado();
        if (!temPerfil(perfilMinimo)) {
            throw new SessaoException("Acesso negado. Perfil necessário: "
                    + perfilMinimo + ". Seu perfil: " + claimsAtual.perfil);
        }
    }

    public String getToken() {
        assertAutenticado();
        return tokenAtual;
    }

    public long getSegundosRestantes() {
        if (!isAutenticado()) return 0;
        return claimsAtual.segundosRestantes();
    }

    // --- interno ---

    private void assertAutenticado() {
        if (!isAutenticado()) {
            throw new SessaoException("Nenhuma sessão ativa. Faça login para continuar.");
        }
    }

    private boolean temPerfil(Perfil minimo) {
        return nivelPerfil(claimsAtual.perfil) >= nivelPerfil(minimo);
    }

    private int nivelPerfil(Perfil perfil) {
        return switch (perfil) {
            case ADMIN -> 2;
            case GERENTE -> 1;
            case MOTORISTA -> 0;
        };
    }

    // --- erro de uso sem login ---

    public static class SessaoException extends RuntimeException {
        public SessaoException(String message) { super(message); }
    }
}
