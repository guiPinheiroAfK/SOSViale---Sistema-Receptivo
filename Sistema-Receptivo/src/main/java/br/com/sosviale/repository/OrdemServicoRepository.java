package br.com.sosviale.repository;

import br.com.sosviale.model.OrdemServico;
import jakarta.persistence.EntityManager;
import java.util.List;

// OS: em vem de fora (quem chama mantem aberto o que precisar)

public class OrdemServicoRepository {

    private final EntityManager em;

    public OrdemServicoRepository(EntityManager em) {
        if (em == null) throw new IllegalArgumentException("EntityManager não pode ser nulo.");
        this.em = em;
    }

    public void salvar(OrdemServico os) {
        if (os == null) throw new IllegalArgumentException("ordem de serviço não pode ser nula.");
        try {
            em.getTransaction().begin();
            em.persist(os);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }

    public OrdemServico buscarPorId(Integer id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("ID inválido.");
        return em.find(OrdemServico.class, id);
    }

    public List<OrdemServico> listarTodos() {
        return em.createQuery("SELECT o FROM OrdemServico o ORDER BY o.dataServico DESC", OrdemServico.class)
                .getResultList();
    }

    public void atualizar(OrdemServico os) {
        if (os == null || os.getId() == null)
            throw new IllegalArgumentException("ordem de serviço inválida para atualização.");
        try {
            em.getTransaction().begin();
            em.merge(os);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        }
    }
}
