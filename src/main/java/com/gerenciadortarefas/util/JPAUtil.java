package com.gerenciadortarefas.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {
    
    private static EntityManagerFactory factory;
    
    static {
        if (factory == null) {
            factory = Persistence.createEntityManagerFactory("gerenciadorTarefasPU");
        }
    }
    
    public static EntityManager getEntityManager() {
        return factory.createEntityManager();
    }
    
    public static void close() {
        if (factory != null) {
            factory.close();
        }
    }
}
