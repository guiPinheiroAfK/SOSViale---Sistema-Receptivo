package br.com.sosviale.util;

import br.com.sosviale.model.*;
import br.com.sosviale.repository.*;
import br.com.sosviale.service.Moeda;
import br.com.sosviale.service.StatusTransfer;
import org.jline.reader.LineReader;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Agendamentos {

    // repositórios injetados pelo construtor
    private PassageiroRepository passageiroRepo;
    private VeiculoRepository veiculoRepo;
    private TransferRepository transferRepo;
    private MotoristaRepository motoristaRepo;
    private final PontoColetaRepository pontoColetaRepo;
    private OrdemServicoRepository osRepo;

    // construtor que recebe os repositórios instanciados pela Main
    public Agendamentos(PassageiroRepository passageiroRepo, VeiculoRepository veiculoRepo,
                       TransferRepository transferRepo, MotoristaRepository motoristaRepo,
                       PontoColetaRepository pontoColetaRepo, OrdemServicoRepository osRepo) {
        this.passageiroRepo = passageiroRepo;
        this.veiculoRepo = veiculoRepo;
        this.transferRepo = transferRepo;
        this.motoristaRepo = motoristaRepo;
        this.pontoColetaRepo = pontoColetaRepo;
        this.osRepo = osRepo;
    }

    // Transfers
    public void agendarTransfer(LineReader reader) {
        System.out.println("\n\u001B[36m--- NOVO AGENDAMENTO DE TRANSFER --- \u001B[0m");
        try {
            String origem = AuxiliarUtils.lerStringObrigatoria(reader, "Origem (Ex: Aeroporto IGU): ");
            String destino = AuxiliarUtils.lerStringObrigatoria(reader, "Destino (Ex: Hotel Cataratas): ");

            if (origem.equalsIgnoreCase(destino)) {
                System.out.println("\u001B[31m[ERRO]: origem e destino não podem ser iguais.\u001B[0m");
                return;
            }

            // moeda e valor
            System.out.println("Moeda: [1] Real (R$)  [2] Dólar (US$)  [3] Guarani (₲)");
            String moedaOpc = reader.readLine("Escolha: ").trim();

            Moeda moeda = switch (moedaOpc) {
                case "2" -> Moeda.USD;
                case "3" -> Moeda.PYG;
                default  -> Moeda.BRL;
            };

            BigDecimal valorInformado = AuxiliarUtils.lerBigDecimalValido(reader, "Valor (" + moeda.getSimbolo() + "): ");
            BigDecimal valorEmReais = CotacaoService.converter(valorInformado, moeda);

            // data e hora — depois do valor
            LocalDateTime dataHora = AuxiliarUtils.lerDataHoraValida(reader, "Data e Hora (dd/MM/yyyy HH:mm): ");

            // criação do transfer com valor já em BRL
            Transfer novoTransfer = new Transfer(dataHora, origem, destino, valorEmReais);
            novoTransfer.setStatus(StatusTransfer.AGENDADO);
            novoTransfer.setMoedaOrigem(moeda);

            // exibe passageiros existentes se solicitado
            String ver2 = reader.readLine("Deseja listar passageiros cadastrados? (s/n): ");
            if (ver2.equalsIgnoreCase("s")) {
                // cria a instância de Listagens passando os repositórios que já estão no Agendamentos
                Listagens listagens = new Listagens(passageiroRepo, veiculoRepo, transferRepo, motoristaRepo, pontoColetaRepo, osRepo);
                // chama o metodo a partir da instância
                listagens.listarPassageiros();
            }

            System.out.println("\n\u001B[33m--- ADICIONANDO PASSAGEIROS --- \u001B[0m");
            System.out.println("A capacidade será validada ao atribuir o veículo na OS.");

            boolean adicionando = true;
            while (adicionando) {
                String pIdStr = reader.readLine("ID do Passageiro (ou 'fim'): ").trim();
                if (pIdStr.equalsIgnoreCase("fim")) {
                    if (novoTransfer.getPassageiros().isEmpty()) {
                        System.out.println("\u001B[31mErro: informe pelo menos 1 passageiro!\u001B[0m");
                        continue;
                    }
                    adicionando = false;
                } else {
                    if (!AuxiliarUtils.isApenasNumeros(pIdStr)) {
                        System.out.println("\u001B[31mID inválido! Digite apenas números.\u001B[0m");
                        continue;
                    }
                    Passageiro p = passageiroRepo.buscarPorId(Long.parseLong(pIdStr));
                    if (p == null) {
                        System.out.println("\u001B[31mPassageiro não encontrado.\u001B[0m");
                    } else if (novoTransfer.getPassageiros().contains(p)) {
                        System.out.println("\u001B[31mPassageiro já adicionado!\u001B[0m");
                    } else {
                        novoTransfer.getPassageiros().add(p);
                        System.out.println("\u001B[32m✔ " + p.getNome() + " adicionado.\u001B[0m");
                    }
                }
            }

            transferRepo.salvar(novoTransfer);
            System.out.println("\n\u001B[32m✔ Transfer [" + novoTransfer.getStatus() + "] salvo e aguardando Ordem de Serviço!\u001B[0m");

            // cadastro opcional de pontos de coleta intermediários
            System.out.println("\n--- PONTOS DE COLETA (opcional) ---");
            System.out.println("Digite 'fim' para encerrar.");
            int ordem = 1;
            while (true) {
                String local = reader.readLine("Parada " + ordem + " - Local (ou 'fim'): ").trim();
                if (local.equalsIgnoreCase("fim")) break;
                if (local.isEmpty()) {
                    System.out.println("\u001B[31mNome do local não pode ser vazio.\u001B[0m");
                    continue;
                }
                String horario = reader.readLine("Horário previsto (HH:mm, ou Enter para pular): ").trim();

                PontoColeta ponto = new PontoColeta();
                ponto.setTransfer(novoTransfer);
                ponto.setLocalColeta(local);
                ponto.setOrdemParada(ordem++);
                if (!horario.isBlank()) {
                    try {
                        ponto.setHorarioPrevisto(LocalTime.parse(horario, DateTimeFormatter.ofPattern("HH:mm")));
                    } catch (DateTimeParseException e) {
                        System.out.println("\u001B[33m[AVISO]: horário inválido, ponto salvo sem horário previsto.\u001B[0m");
                    }
                }
                // coordenadas zeradas até integração com geocodificação
                ponto.setLatitude(0.0);
                ponto.setLongitude(0.0);
                pontoColetaRepo.salvar(ponto);
                System.out.println("\u001B[32m✔ Ponto de coleta salvo.\u001B[0m");
            }

        } catch (Exception e) {
            System.out.println("\u001B[31m[ERRO]: " + e.getMessage() + "\u001B[0m");
        }
    }

    public class CotacaoService {

        // Cotações fixas de fallback — idealmente viriam de uma API
        private static final BigDecimal COTACAO_USD = new BigDecimal("5.75");
        private static final BigDecimal COTACAO_PYG = new BigDecimal("0.0055");

        public static BigDecimal converter(BigDecimal valor, Moeda moeda) {
            return switch (moeda) {
                case BRL -> valor;
                case USD -> valor.multiply(COTACAO_USD).setScale(2, RoundingMode.HALF_UP);
                case PYG -> valor.multiply(COTACAO_PYG).setScale(2, RoundingMode.HALF_UP);
            };
        }
    }

}
