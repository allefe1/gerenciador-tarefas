package com.gerenciadortarefas.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class TestJPAUtil {
    private static EntityManagerFactory factory;
    
    static {
        factory = Persistence.createEntityManagerFactory("gerenciadorTarefasPUTest");
    }
    
    public static EntityManager getEntityManager() {
        return factory.createEntityManager();
    }
}
