package br.com.sosviale.cli;

import br.com.sosviale.config.DbConfig;
import br.com.sosviale.repository.*;
import br.com.sosviale.service.MenuService;
import br.com.sosviale.repository.OrdemServicoRepository;
import br.com.sosviale.config.JPAUtil;
import jakarta.persistence.EntityManager;

public class Main {
    public static void main(String[] args) {

        // inicializa o flyway para criar/atualizar as tabelas no banco
        try {
            System.out.println("Iniciando migrações do Flyway...");
            DbConfig.setup();
            System.out.println("Flyway: tabelas atualizadas com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao executar migrações do Flyway: " + e.getMessage());
            // se o flyway falhar, encerra sem tentar subir o resto da aplicação
            return;
        }

        EntityManager em = JPAUtil.getEntityManager();

        // instancia os repositórios responsáveis pelo acesso ao banco
        PassageiroRepository passageiroRepo = new PassageiroRepository();
        VeiculoRepository veiculoRepo = new VeiculoRepository();
        TransferRepository transferRepo = new TransferRepository();
        MotoristaRepository motoristaRepo = new MotoristaRepository();
        PontoColetaRepository pontoColetaRepo = new PontoColetaRepository();
        OrdemServicoRepository osRepo = new OrdemServicoRepository(em);

        // cria o serviço de menu injetando todos os repositórios necessários
        MenuService menuService = new MenuService(
                passageiroRepo,
                veiculoRepo,
                transferRepo,
                motoristaRepo,
                pontoColetaRepo,
                osRepo
        );

        // inicia o loop principal do sistema
        menuService.menu();
    }
}
