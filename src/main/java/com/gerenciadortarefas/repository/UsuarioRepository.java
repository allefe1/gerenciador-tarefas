package com.gerenciadortarefas.repository;

import java.io.Serializable; // Importe a interface Serializable
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.util.JPAUtil;

public class UsuarioRepository extends AbstractRepository<Usuario, Long> implements Serializable { // Adicione "implements Serializable"
    
    private static final long serialVersionUID = 1L; // Adicione um serialVersionUID
    
    public Usuario findByEmail(String email) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :email", Usuario.class);
            query.setParameter("email", email);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            if (em != null && em.isOpen()) { // Boa prática verificar se está aberto antes de fechar
                em.close();
            }
        }
    }
    
    public boolean existsWithEmail(String email) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u WHERE u.email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    /**
     * Retorna uma lista de todos os usuários cadastrados no sistema
     * @return Lista de usuários
     */
    public List<Usuario> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("FROM Usuario", Usuario.class).getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}