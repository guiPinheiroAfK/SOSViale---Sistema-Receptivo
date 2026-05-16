package br.com.sosviale.service;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.ParadaOS;
import br.com.sosviale.repository.OrdemServicoRepository;
import br.com.sosviale.service.pathfinding.RouteResult;
import br.com.sosviale.util.PathFindingUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class OrdemServicoService {

    public void cadastrar(OrdemServico os) {
        if (os == null) throw new IllegalArgumentException("Ordem de serviço não pode ser nula.");
        if (os.getMotorista() == null) throw new IllegalArgumentException("Motorista é obrigatório.");
        if (os.getVeiculo() == null) throw new IllegalArgumentException("Veículo é obrigatório.");

        EntityManager em = JPAUtil.getEntityManager();
        try {
            new OrdemServicoRepository(em).salvar(os);
        } finally {
            em.close();
        }
    }

    public OrdemServico buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return new OrdemServicoRepository(em).buscarPorId(id);
        } finally {
            em.close();
        }
    }

    public List<OrdemServico> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return new OrdemServicoRepository(em).listarTodos();
        } finally {
            em.close();
        }
    }

    public void atualizar(OrdemServico os) {
        if (os == null || os.getId() == null)
            throw new IllegalArgumentException("Ordem de serviço inválida para atualização.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            new OrdemServicoRepository(em).atualizar(os);
        } finally {
            em.close();
        }
    }

    /**
     * Otimiza a rota da Ordem de Serviço, gera as paradas agrupadas e salva no banco.
     *
     * @param osId ID da Ordem de Serviço
     * @param usarGps true para usar cálculo OSRM com posição do motorista, false para Haversine
     */
    public void montarRotaOtimizada(Integer osId, boolean usarGps) {
        if (osId == null || osId <= 0) throw new IllegalArgumentException("ID da OS inválido.");

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // 1. Busca a OS já anexada no contexto do JPA
            OrdemServico os = em.find(OrdemServico.class, osId);
            if (os == null) {
                throw new RuntimeException("Ordem de Serviço #" + osId + " não encontrada.");
            }

            // 2. Roda o algoritmo puro (delegando para o utilitário que criamos)
            RouteResult resultado;
            if (usarGps) {
                resultado = PathFindingUtil.otimizarComGps(os, os.getMotorista());
            } else {
                resultado = PathFindingUtil.otimizar(os);
            }

            // 3. Converte o resultado matemático em entidades de negócio (ParadaOS)
            List<ParadaOS> novasParadas = PathFindingUtil.gerarParadasOS(resultado, os);

            // 4. Limpa as paradas antigas (o orphanRemoval = true vai apagar do banco)
            os.getParadasRota().clear();

            // 5. Adiciona a nova sequência otimizada
            os.getParadasRota().addAll(novasParadas);

            // 6. Atualiza a OS (isso faz o cascade salvar as novas ParadaOS e tabela pivot)
            em.merge(os);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Falha ao otimizar rota da OS: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}