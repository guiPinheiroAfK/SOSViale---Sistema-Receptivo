package br.com.sosviale.repository;

import br.com.sosviale.model.OrdemServico;
import jakarta.persistence.EntityManager;
import java.util.List;

public class OrdemServicoRepository {

    private final EntityManager em;

    public OrdemServicoRepository(EntityManager em) {
        this.em = em;
    }

    public void salvar(OrdemServico os) {
        em.getTransaction().begin();
        em.persist(os);
        em.getTransaction().commit();
    }

    public OrdemServico buscarPorId(Integer id) {
        return em.find(OrdemServico.class, id);
    }

    public List<OrdemServico> listarTodos() {
        return em.createQuery("SELECT o FROM OrdemServico o", OrdemServico.class).getResultList();
    }

    public void atualizar(OrdemServico os) {
        em.getTransaction().begin();
        em.merge(os);
        em.getTransaction().commit();
    }
}