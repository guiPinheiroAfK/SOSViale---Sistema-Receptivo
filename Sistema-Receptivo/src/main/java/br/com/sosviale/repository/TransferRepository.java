package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.Transfer;
import jakarta.persistence.EntityManager;
import java.util.List;

/*
 * DAO responsável pelas operações de persistência da entidade Transfer.
 * Atualizado para refletir a separação de data e hora.
 */
public class TransferRepository {

    public void salvar(Transfer transfer) {
        if (transfer == null) throw new IllegalArgumentException("transfer não pode ser nulo.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(transfer);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("erro ao cadastrar transfer: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /*
     * Retorna todos os transfers ordenados por data e depois por hora.
     */
    public List<Transfer> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Atualizado: agora ordena primeiro pela data e depois pela hora
            return em.createQuery("SELECT t FROM Transfer t ORDER BY t.dataTransfer ASC, t.horaTransfer ASC", Transfer.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public void atualizar(Transfer transfer) {
        if (transfer == null || transfer.getId() == null)
            throw new IllegalArgumentException("transfer inválido para atualização.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(transfer);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // Ajustado para Integer para coincidir com a Entidade
    public void excluir(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido para exclusão.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Transfer t = em.find(Transfer.class, id);
            if (t != null) {
                em.remove(t);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public int contarPassageirosPorVeiculo(Integer veiculoId) {
        if (veiculoId == null || veiculoId <= 0)
            throw new IllegalArgumentException("ID de veículo inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long total = em.createQuery(
                            "SELECT COUNT(p) FROM Transfer t JOIN t.passageiros p " +
                                    "WHERE t.ordemServico.veiculo.id = :veiculoId AND t.status = 'AGENDADO'",
                            Long.class)
                    .setParameter("veiculoId", veiculoId)
                    .getSingleResult();
            return total.intValue();
        } finally {
            em.close();
        }
    }

    public Transfer buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Transfer.class, id);
        } finally {
            em.close();
        }
    }

    public long contarSemOrdemServico() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT COUNT(t) FROM Transfer t WHERE t.ordemServico IS NULL", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}