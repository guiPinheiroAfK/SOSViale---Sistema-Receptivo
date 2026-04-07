package br.com.sosviale.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    // Criacao de apenas UMA fábrica para o projeto todo (Static)
    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("sos-viale-pu");

    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }
}