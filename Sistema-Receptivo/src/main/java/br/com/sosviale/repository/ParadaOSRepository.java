package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.ParadaOS;
import jakarta.persistence.EntityManager;

public class ParadaOSRepository {

    // Atualiza os dados de uma parada (ex: quando o motorista altera o status para 'CONCLUIDO')
    public void atualizar(ParadaOS parada) {
        if (parada == null || parada.getId() == null) {
            throw new IllegalArgumentException("Parada inválida para atualização.");
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(parada);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Erro ao atualizar a parada: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // Busca uma parada específica pelo ID
    public ParadaOS buscarPorId(Integer id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID inválido.");
        }
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(ParadaOS.class, id);
        } finally {
            em.close();
        }
    }
}