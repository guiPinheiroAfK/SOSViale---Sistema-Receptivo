package br.com.sosviale.controller.veiculo.dto;

public record VeiculoRequest(
        Integer id,
        String label,
        String placa,
        int capacidade,
        String marca,
        String tipo
) {}
