package br.com.sosviale.offline.dto;

// login salvo em session.json pra bootstrap offline

public class OfflineSessionDto {

    private String usuario;
    private String nome;
    private String perfil;
    private boolean admin;

    public OfflineSessionDto() {}

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public boolean isAdmin() { return admin; }
    public void setAdmin(boolean admin) { this.admin = admin; }
}
