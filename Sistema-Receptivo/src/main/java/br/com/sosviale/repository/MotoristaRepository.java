package br.com.sosviale.repository;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.config.JPAUtil; // Importa a utilitária
import jakarta.persistence.EntityManager;
import java.util.List;

public class MotoristaRepository {

    public void salvar(Motorista motorista) {
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
            return em.createQuery("SELECT m FROM Motorista m", Motorista.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Motorista buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Motorista.class, id);
        } finally {
            em.close();
        }
    }
}