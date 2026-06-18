package br.com.sosviale.controller.pontoColeta.dto;

public record PontoColetaRequest(
        Long id,
        String localColeta,
        Double latitude,
        Double longitude
) {}
