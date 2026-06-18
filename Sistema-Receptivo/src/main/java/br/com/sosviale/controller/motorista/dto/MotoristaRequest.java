package br.com.sosviale.controller.motorista.dto;

public record MotoristaRequest(
        Integer id,
        String nome,
        String cnh,
        String telefone
) {}
