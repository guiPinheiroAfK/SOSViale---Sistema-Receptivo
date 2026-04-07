package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.PontoColeta;
import jakarta.persistence.EntityManager;
import java.util.List;

public class PontoColetaRepository {

    public void salvar(PontoColeta ponto) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(ponto);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Erro ao salvar Ponto de Coleta: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Busca todos os pontos de coleta de um transfer específico,
     * ordenados pela sequência de parada.
     */
    public List<PontoColeta> buscarPorTransfer(Integer transferId) {
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
}