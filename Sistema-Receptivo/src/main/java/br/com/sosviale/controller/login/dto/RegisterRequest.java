package br.com.sosviale.controller.login.dto;

import br.com.sosviale.model.Perfil;

public record RegisterRequest(
        String nome,
        String username,
        String password,
        String adminPassword,
        Perfil perfil
) {}
