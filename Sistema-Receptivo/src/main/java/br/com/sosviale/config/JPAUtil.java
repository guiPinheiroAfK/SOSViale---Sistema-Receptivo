package br.com.sosviale.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

// fabrica PU unica; quem pegar EM tem que dar close

public class JPAUtil {

    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("sos-viale-pu");

    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }
}