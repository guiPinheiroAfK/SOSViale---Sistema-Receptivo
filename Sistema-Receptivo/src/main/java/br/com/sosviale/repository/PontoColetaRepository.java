package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.PontoColeta;
import jakarta.persistence.EntityManager;
import java.util.List;

// DAO responsável pelas operações de persistência da entidade PontoColeta
public class PontoColetaRepository {

    // persiste um novo ponto de coleta no banco
    public void salvar(PontoColeta ponto) {
        if (ponto == null) throw new IllegalArgumentException("ponto de coleta não pode ser nulo.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ponto);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("erro ao salvar ponto de coleta: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    // busca todos os pontos de coleta de um transfer específico, ordenados pela sequência de parada
    public List<PontoColeta> buscarPorTransfer(Integer transferId) {
        if (transferId == null || transferId <= 0)
            throw new IllegalArgumentException("ID de transfer inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT p FROM PontoColeta p WHERE p.transfer.id = :tId ORDER BY p.ordemParada ASC",
                            PontoColeta.class)
                    .setParameter("tId", transferId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // atualiza os dados de um ponto de coleta existente
    public void atualizar(PontoColeta ponto) {
        if (ponto == null || ponto.getId() == null)
            throw new IllegalArgumentException("ponto de coleta inválido para atualização.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(ponto);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    // remove um ponto de coleta pelo ID; ignorado silenciosamente se não existir
    public void excluir(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido para exclusão.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            PontoColeta p = em.find(PontoColeta.class, id);
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

    // busca um ponto de coleta pelo ID; retorna null se não encontrado
    public PontoColeta buscarPorId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(PontoColeta.class, id);
        } finally {
            em.close();
        }
    }
}