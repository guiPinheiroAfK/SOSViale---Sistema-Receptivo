package br.com.sosviale.controller.transfer.dto;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.service.Moeda;
import br.com.sosviale.service.StatusTransfer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record TransferRequest(
        Integer id,
        String origem,
        String destino,
        LocalDate data,
        LocalTime hora,
        BigDecimal valorOriginal,
        Moeda moeda,
        StatusTransfer status,
        List<Passageiro> passageiros
) {}
