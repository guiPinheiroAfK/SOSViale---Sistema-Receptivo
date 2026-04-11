package br.com.sosviale.repository;

import br.com.sosviale.config.JPAUtil;
import br.com.sosviale.model.Veiculo;
import jakarta.persistence.EntityManager;
import java.util.List;

// DAO responsável pelas operações de persistência da entidade Veiculo
public class VeiculoRepository {

    // persiste um novo veículo no banco
    public void salvar(Veiculo veiculo) {
        if (veiculo == null) throw new IllegalArgumentException("veículo não pode ser nulo.");
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

    // retorna todos os veículos cadastrados
    public List<Veiculo> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT v FROM Veiculo v ORDER BY v.label ASC", Veiculo.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // busca um veículo pelo ID; retorna null se não encontrado
    public Veiculo buscarPorId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Veiculo.class, id);
        } finally {
            em.close();
        }
    }

    // atualiza os dados de um veículo existente
    public void atualizar(Veiculo veiculo) {
        if (veiculo == null || veiculo.getId() == null)
            throw new IllegalArgumentException("veículo inválido para atualização.");
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

    // remove o veículo com o ID informado; ignorado silenciosamente se não existir
    public void excluir(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido para exclusão.");
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