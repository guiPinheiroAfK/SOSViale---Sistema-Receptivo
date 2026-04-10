package br.com.sosviale.repository;

import br.com.sosviale.model.Passageiro;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;

public class PassageiroRepository {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("sos-viale-pu");

    //Salva um passageiro no banco.
    public void salvar(Passageiro passageiro) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(passageiro); // O Hibernate gera o INSERT para você
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            System.err.println("Erro ao salvar passageiro: " + e.getMessage());
            throw e;
        } finally {
            em.close();
        }
    }


    // Lista todos os passageiros.
    public List<Passageiro> listarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            // JPQL (Java Persistence Query Language) referenciando a Classe, não a Tabela
            return em.createQuery("SELECT p FROM Passageiro p", Passageiro.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Passageiro buscarPorId(Long id) {
        EntityManager em = emf.createEntityManager();
        return em.find(Passageiro.class, id);
    }

    public void atualizar(Passageiro passageiro) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(passageiro); // O merge sincroniza o objeto com o banco
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void excluir(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Passageiro p = em.find(Passageiro.class, id);
            if (p != null) {
                em.remove(p); // Remove o registro do banco
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