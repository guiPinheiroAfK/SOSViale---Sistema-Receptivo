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
            // Usamos JPQL (Java Persistence Query Language) referenciando a Classe, não a Tabela
            return em.createQuery("SELECT p FROM Passageiro p", Passageiro.class).getResultList();
        } finally {
            em.close();
        }
    }

    // Futuramente você pode adicionar buscarPorId, atualizar e deletar aqui para fechar o CRUD
}