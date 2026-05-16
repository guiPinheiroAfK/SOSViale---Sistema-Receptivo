package br.com.sosviale.service;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.OrdemServico;
import br.com.sosviale.model.ParadaOS;
import br.com.sosviale.model.Transfer;
import br.com.sosviale.repository.OrdemServicoRepository;
import br.com.sosviale.service.pathfinding.RouteResult;
import br.com.sosviale.util.PathFindingUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

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

    /*busca uma OS pelo ID de forma simples (objeto detached ao retornar).
     Os transfers são carregados pelo FetchType.EAGER definido na entidade,
     mas use buscarComTransfers() quando precisar de uma lista garantidamente
     fresca após operações de vinculação.*/
    public OrdemServico buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return new OrdemServicoRepository(em).buscarPorId(id);
        } finally {
            em.close();
        }
    }

    // busca uma OS com seus transfers carregados via JOIN FETCH explícito.

    public OrdemServico buscarComTransfers(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            OrdemServico os = em.createQuery(
                    "SELECT DISTINCT o FROM OrdemServico o " +
                            "LEFT JOIN FETCH o.transfers " +
                            "WHERE o.id = :id",
                    OrdemServico.class
            ).setParameter("id", id).getSingleResult();

            // hibernate não permite JOIN FETCH em duas List (bags) na mesma query
            // carrega passageiros em uma segunda consulta, com a sessão ainda aberta
            if (!os.getTransfers().isEmpty()) {
                em.createQuery(
                        "SELECT DISTINCT t FROM Transfer t " +
                                "LEFT JOIN FETCH t.passageiros " +
                                "WHERE t.ordemServico.id = :osId",
                        Transfer.class
                ).setParameter("osId", id).getResultList();
            }

            return os;
        } catch (NoResultException e) {
            return null;
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

    //otimiza a rota da Ordem de Serviço, gera as paradas agrupadas e salva no banco.

    public void montarRotaOtimizada(Integer osId, boolean usarGps) {
        if (osId == null || osId <= 0) throw new IllegalArgumentException("ID da OS inválido.");

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            OrdemServico os = em.find(OrdemServico.class, osId);
            if (os == null)
                throw new RuntimeException("Ordem de Serviço #" + osId + " não encontrada.");

            RouteResult resultado;
            if (usarGps) {
                resultado = PathFindingUtil.otimizarComGps(os, os.getMotorista());
            } else {
                resultado = PathFindingUtil.otimizar(os);
            }

            List<ParadaOS> novasParadas = PathFindingUtil.gerarParadasOS(resultado, os);

            os.getParadasRota().clear();
            os.getParadasRota().addAll(novasParadas);

            em.merge(os);
            em.getTransaction().commit();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Falha ao otimizar rota da OS: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}