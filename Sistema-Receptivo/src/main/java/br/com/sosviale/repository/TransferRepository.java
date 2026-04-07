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
}