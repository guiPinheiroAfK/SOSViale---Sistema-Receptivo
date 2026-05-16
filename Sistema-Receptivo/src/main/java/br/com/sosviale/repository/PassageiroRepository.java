package br.com.sosviale.repository;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.config.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

// passageiro: crud padrao

public class PassageiroRepository {

    public void salvar(Passageiro passageiro) {
        if (passageiro == null) throw new IllegalArgumentException("passageiro não pode ser nulo.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(passageiro);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("erro ao cadastrar passageiro: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Passageiro> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Passageiro p ORDER BY p.nome ASC", Passageiro.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Passageiro buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Passageiro.class, id);
        } finally {
            em.close();
        }
    }

    public void atualizar(Passageiro passageiro) {
        if (passageiro == null || passageiro.getId() == null)
            throw new IllegalArgumentException("passageiro inválido para atualização.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(passageiro);
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
            Passageiro p = em.find(Passageiro.class, id);
            if (p != null) {
                em.remove(p);
            }
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
            return em.createQuery("SELECT COUNT(p) FROM Passageiro p", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
