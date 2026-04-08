package br.com.sosviale;

import br.com.sosviale.config.DbConfig;
import br.com.sosviale.model.*;
import br.com.sosviale.repository.*;
import br.com.sosviale.service.MenuService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        //O Flyway prepara o banco (Cria/Atualiza as tabelas)
        try {
            System.out.println("Iniciando migrações do Flyway...");
            DbConfig.setup();
            System.out.println("Flyway: Tabelas atualizadas!");
        } catch (Exception e) {
            System.err.println("Erro no Flyway: " + e.getMessage());
            // Se o Flyway falhar ele nao tenta o resto
            return;
        }

        //teste
        try {
            // 1. Instanciar Repositórios
            MotoristaRepository motoristaRepo = new MotoristaRepository();
            VeiculoRepository veiculoRepo = new VeiculoRepository();
            PassageiroRepository passageiroRepo = new PassageiroRepository();
            TransferRepository transferRepo = new TransferRepository();
            PontoColetaRepository pontoRepo = new PontoColetaRepository();
            MenuService menuService = new MenuService(passageiroRepo, veiculoRepo);

            menuService.iniciar();

            //Criar e Salvar Infraestrutura (Motorista e Veículo)
            Motorista m1 = new Motorista("Sancho Pança", "CNH-124356");
            motoristaRepo.salvar(m1);

            Veiculo v1 = new Veiculo("Van Luxo", "SOS-2023", 15);
            veiculoRepo.salvar(v1);

            //Criar e Salvar Passageiros
            Passageiro p1 = new Passageiro("Guilherme Gocks", "DOC-999", "Brasileira");
            Passageiro p2 = new Passageiro("Miguel de Cervantes", "DOC-777", "Espanhola");
            passageiroRepo.salvar(p1);
            passageiroRepo.salvar(p2);

            //Criar o Transfer e Associar Passageiros (ManyToMany)
            Transfer t1 = new Transfer(
                    LocalDateTime.now().plusDays(1), // Amanhã
                    "Aeroporto IGU",
                    "Hotel das Cataratas",
                    new java.math.BigDecimal("250.00"),
                    m1,
                    v1
            );

            // Adicionando passageiros à lista do transfer
            t1.getPassageiros().add(p1);
            t1.getPassageiros().add(p2);

            // Salvar o Transfer
            transferRepo.salvar(t1);

            //Criar e Salvar Pontos de Coleta (Logística)
            PontoColeta pc1 = new PontoColeta(t1, "Parada para Câmbio", 1, java.time.LocalTime.of(10, 30));
            pontoRepo.salvar(pc1);

            System.out.println("\n=== TESTE DE LOGÍSTICA CONCLUÍDO COM SUCESSO! ===");
            System.out.println("Transfer ID: " + t1.getId());
            System.out.println("Passageiros no transfer: " + t1.getPassageiros().size());
            System.out.println("Ponto de coleta salvo para o local: " + pc1.getLocalColeta());

        } catch (Exception e) {
            System.err.println("ERRO NO TESTE: " + e.getMessage());
            e.printStackTrace();
        }


    }
    }