package br.com.sosviale.repository;

import br.com.sosviale.model.Motorista;
import br.com.sosviale.config.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

// DAO responsável pelas operações de persistência da entidade Motorista
public class MotoristaRepository {

    // persiste um novo motorista no banco; lança exceção em caso de falha
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

    // retorna todos os motoristas cadastrados; lista vazia se não houver nenhum
    public List<Motorista> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT m FROM Motorista m ORDER BY m.nome ASC", Motorista.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // busca um motorista pelo ID; retorna null se não encontrado
    public Motorista buscarPorId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Motorista.class, id);
        } finally {
            em.close();
        }
    }

    // atualiza os dados de um motorista existente
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

    // remove o motorista com o ID informado; ignorado silenciosamente se não existir
    public void excluir(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido para exclusão.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Motorista m = em.find(Motorista.class, id);
            if (m != null) {
                em.remove(m);
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