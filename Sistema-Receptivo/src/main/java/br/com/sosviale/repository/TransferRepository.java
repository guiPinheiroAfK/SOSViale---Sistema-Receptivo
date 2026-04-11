package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.Transfer;
import jakarta.persistence.EntityManager;
import java.util.List;

// DAO responsável pelas operações de persistência da entidade Transfer
public class TransferRepository {

    // persiste um novo transfer no banco
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
            System.err.println("erro ao salvar transfer: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    // retorna todos os transfers cadastrados
    public List<Transfer> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM Transfer t ORDER BY t.dataHora ASC", Transfer.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // atualiza os dados de um transfer existente (origem, destino, valor, status, etc.)
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

    // remove um transfer pelo ID; ignorado silenciosamente se não existir
    public void excluir(Long id) {
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

    // conta o total de passageiros em transfers agendados para um determinado veículo
    public int contarPassageirosPorVeiculo(Long veiculoId) {
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

    // busca um transfer pelo ID; retorna null se não encontrado
    public Transfer buscarPorId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Transfer.class, id);
        } finally {
            em.close();
        }
    }
}