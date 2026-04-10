package br.com.sosviale.cli;

import br.com.sosviale.config.DbConfig;
import br.com.sosviale.repository.*;
import br.com.sosviale.service.MenuService;

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

        PassageiroRepository passageiroRepo = new PassageiroRepository();
        VeiculoRepository veiculoRepo = new VeiculoRepository();
        TransferRepository transferRepo = new TransferRepository();
        MotoristaRepository motoristaRepo = new MotoristaRepository();
        PontoColetaRepository pontoColetaRepo = new PontoColetaRepository();

        // 2. Cria o serviço do menu passando os repositórios
        MenuService menuService = new MenuService(
                passageiroRepo,
                veiculoRepo,
                transferRepo,
                motoristaRepo,
                pontoColetaRepo

        );

        // 3. Liga o motor!
        menuService.menu();
    }
    }