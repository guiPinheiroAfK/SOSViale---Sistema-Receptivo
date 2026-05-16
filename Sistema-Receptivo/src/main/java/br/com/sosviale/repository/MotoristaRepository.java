package br.com.sosviale.repository;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.config.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

// crud motorista: sempre abre transaction local e fecha o em

public class MotoristaRepository {

    public void salvar(Motorista motorista) {
        if (motorista == null) throw new IllegalArgumentException("motorista não pode ser nulo.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(motorista);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Motorista> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT m FROM Motorista m ORDER BY m.nome ASC", Motorista.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Motorista buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Motorista.class, id);
        } finally {
            em.close();
        }
    }

    public void atualizar(Motorista motorista) {
        if (motorista == null || motorista.getId() == null)
            throw new IllegalArgumentException("motorista inválido para atualização.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(motorista);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void excluir(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido para exclusão.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Motorista m = em.find(Motorista.class, id);
            if (m != null) em.remove(m);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public long contar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(m) FROM Motorista m", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
