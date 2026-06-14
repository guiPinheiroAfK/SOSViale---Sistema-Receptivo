package br.com.sosviale.controller.passageiro.dto;

import br.com.sosviale.model.TipoDocumento;

public record PassageiroRequest(
        Integer id,
        String nome,
        String documento,
        TipoDocumento tipoDocumento,
        String nacionalidade
) {}
