package br.com.sosviale.repository;

import br.com.sosviale.model.User;
import br.com.sosviale.config.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

public class UserRepository {

    public User buscarPorUsuario(String usuario) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<User> resultado = em.createQuery(
                            "SELECT u FROM User u WHERE u.usuario = :usuario", User.class)
                    .setParameter("usuario", usuario)
                    .getResultList();
            return resultado.isEmpty() ? null : resultado.get(0);
        } finally {
            em.close();
        }
    }

    public User buscarAdmin() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<User> resultado = em.createQuery(
                            "SELECT u FROM User u WHERE u.isAdmin = true", User.class)
                    .getResultList();
            return resultado.isEmpty() ? null : resultado.get(0);
        } finally {
            em.close();
        }
    }

    public void salvar(User user) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void excluir(String usuario) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            User u = buscarPorUsuario(usuario);
            if (u != null) em.remove(em.merge(u));
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<User> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u ORDER BY u.nome ASC", User.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}