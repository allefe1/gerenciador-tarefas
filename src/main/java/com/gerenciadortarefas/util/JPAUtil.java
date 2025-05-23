package com.gerenciadortarefas.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JPAUtil {

    private static EntityManagerFactory factory;
    private static final String PERSISTENCE_UNIT_NAME = "gerenciadorTarefasPU"; // Nome da sua unidade de persistência

    static {
        try {
            String databaseUrlEnv = System.getenv("DATABASE_URL");

            if (databaseUrlEnv != null && !databaseUrlEnv.isEmpty()) {
                System.out.println("JPAUtil: DATABASE_URL encontrada: " + databaseUrlEnv);
                System.out.println("JPAUtil: Configurando EntityManagerFactory programaticamente para Heroku...");

                URI dbUri = new URI(databaseUrlEnv);

                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String dbPath = dbUri.getPath();
                // Remover a barra inicial do path se existir, para o nome do banco
                String dbName = (dbPath.startsWith("/")) ? dbPath.substring(1) : dbPath;

                String jdbcUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + "/" + dbName;
                
                // Garantir SSL para conexões Heroku (não localhost)
                if (!"localhost".equals(dbUri.getHost())) {
                    String query = dbUri.getQuery();
                    if (query != null && query.contains("sslmode=")) {
                        // Se sslmode já está na query original da DATABASE_URL, use-a
                        jdbcUrl += "?" + query;
                    } else if (query != null) {
                        // Se há query, mas não sslmode, adicione sslmode=require
                        jdbcUrl += "?" + query + "&sslmode=require";
                    } else {
                        // Se não há query, adicione sslmode=require
                        jdbcUrl += "?sslmode=require";
                    }
                } else if (dbUri.getQuery() != null) { // Para localhost, apenas adicione a query se existir
                     jdbcUrl += "?" + dbUri.getQuery();
                }

                System.out.println("JPAUtil: JDBC URL construída: " + jdbcUrl);
                System.out.println("JPAUtil: Usuário: " + username);
                // Não vamos logar a senha

                Map<String, String> properties = new HashMap<>();
                properties.put("javax.persistence.jdbc.driver", "org.postgresql.Driver");
                properties.put("javax.persistence.jdbc.url", jdbcUrl);
                properties.put("javax.persistence.jdbc.user", username);
                properties.put("javax.persistence.jdbc.password", password);

                // As outras propriedades (dialect, hbm2ddl.auto, show_sql, etc.)
                // ainda serão lidas do seu persistence.xml.
                // As propriedades definidas aqui no mapa têm precedência para conexão.

                factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties);

            } else {
                // Fallback: Se DATABASE_URL não estiver definida (ex: ambiente local sem essa variável)
                // Tenta carregar a configuração padrão do persistence.xml
                // (que no seu caso atual está com as credenciais locais comentadas,
                // então para rodar localmente você descomentaria lá ou usaria outra estratégia local)
                System.out.println("JPAUtil: DATABASE_URL não encontrada. Tentando carregar EMF via persistence.xml padrão.");
                factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
            }

            System.out.println("JPAUtil: EntityManagerFactory criado com sucesso!");

        } catch (Throwable ex) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("!!! ERRO GRAVE AO INICIALIZAR O EntityManagerFactory NO JPAUtil !!!");
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            ex.printStackTrace(System.err);
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            throw new RuntimeException("Falha ao inicializar EntityManagerFactory no JPAUtil: " + ex.getMessage(), ex);
        }
    }

    public static EntityManager getEntityManager() {
        if (factory == null) {
            throw new IllegalStateException("EntityManagerFactory não foi inicializada. Verifique os logs para erros na inicialização do JPAUtil.");
        }
        return factory.createEntityManager();
    }

    public static void close() {
        if (factory != null && factory.isOpen()) {
            System.out.println("JPAUtil: Fechando EntityManagerFactory...");
            factory.close();
            System.out.println("JPAUtil: EntityManagerFactory fechado.");
        }
    }
}