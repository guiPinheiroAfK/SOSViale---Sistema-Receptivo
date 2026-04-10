package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.Transfer;
import jakarta.persistence.EntityManager;
import java.util.List;

public class TransferRepository {

    public void salvar(Transfer transfer) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();

            // O em.merge é interessante aqui: se o motorista/veiculo já existem,
            // ele apenas "anexa" eles ao transfer sem tentar criá-los de novo.
            em.persist(transfer);

            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Erro ao salvar Transfer: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Transfer> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // JPQL buscando a entidade com seus relacionamentos
            return em.createQuery("SELECT t FROM Transfer t", Transfer.class).getResultList();
        } finally {
            em.close();
        }
    }

    public void atualizar(Transfer transfer) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(transfer); // Sincroniza as mudanças de origem, destino, valor, etc.
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void excluir(Long id) {
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

    public int contarPassageirosPorVeiculo(Long veiculoId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Long total = em.createQuery(
                            "SELECT COUNT(p) FROM Transfer t JOIN t.passageiros p WHERE t.ordemServico.veiculo.id = :veiculoId AND t.status = 'AGENDADO'",
                            Long.class)
                    .setParameter("veiculoId", veiculoId)
                    .getSingleResult();
            return total.intValue();
        } finally {
            em.close();
        }
    }

    public Transfer buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Transfer.class, id);
        } finally {
            em.close();
        }
    }
}