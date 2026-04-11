package br.com.sosviale.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    // factory estática — apenas uma instância por toda a aplicação (padrão singleton)
    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("sos-viale-pu");

    // retorna um novo EntityManager a cada chamada; o chamador é responsável por fechar
    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }
}