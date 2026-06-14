package br.com.sosviale.controller.usuario.dto;

import br.com.sosviale.model.Perfil;

public record UsuarioRequest(
        String usuario,
        String nome,
        String senha,
        String senhaAdmin,
        Perfil perfil
) {}
