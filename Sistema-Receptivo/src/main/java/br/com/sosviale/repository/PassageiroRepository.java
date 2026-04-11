package br.com.sosviale.repository;

import br.com.sosviale.model.Passageiro;
import br.com.sosviale.config.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

// DAO responsável pelas operações de persistência da entidade Passageiro
public class PassageiroRepository {

    // persiste um novo passageiro no banco
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
            System.err.println("erro ao salvar passageiro: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    // retorna todos os passageiros cadastrados ordenados por nome
    public List<Passageiro> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Passageiro p ORDER BY p.nome ASC", Passageiro.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // busca um passageiro pelo ID; retorna null se não encontrado
    public Passageiro buscarPorId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Passageiro.class, id);
        } finally {
            em.close();
        }
    }

    // atualiza os dados de um passageiro existente
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

    // remove o passageiro com o ID informado; ignorado silenciosamente se não existir
    public void excluir(Long id) {
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
}