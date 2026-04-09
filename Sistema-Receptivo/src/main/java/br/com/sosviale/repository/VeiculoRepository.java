package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.Passageiro;
import br.com.sosviale.model.Veiculo;
import jakarta.persistence.EntityManager;
import java.util.List;

public class VeiculoRepository {

    public void salvar(Veiculo veiculo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(veiculo);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Veiculo> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT v FROM Veiculo v", Veiculo.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Veiculo buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        return em.find(Veiculo.class, id);
    }

    public void atualizar(Veiculo veiculo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(veiculo);
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
            Veiculo v = em.find(Veiculo.class, id);
            if (v != null) {
                em.remove(v);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}