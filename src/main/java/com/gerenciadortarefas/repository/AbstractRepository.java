package com.gerenciadortarefas.repository;

import java.io.Serializable; // Importe a interface Serializable
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import com.gerenciadortarefas.util.JPAUtil;

// Adicione "implements Serializable" à declaração da classe
public abstract class AbstractRepository<T, ID extends Serializable> implements Repository<T, ID>, Serializable {
    
    private static final long serialVersionUID = 1L; // Adicione um serialVersionUID
    
    private Class<T> entityClass;
    
    @SuppressWarnings("unchecked")
    public AbstractRepository() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
    }
    
    @Override
    public T save(T entity) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = null; // Inicialize como null
        
        try {
            tx = em.getTransaction(); // Obtenha a transação aqui
            tx.begin();
            entity = em.merge(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            
            // throw new RuntimeException("Erro ao salvar entidade: " + e.getMessage(), e); 
            throw e; 
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public void remove(T entity) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = null; 
        
        try {
            tx = em.getTransaction(); // Obtenha a transação aqui
            tx.begin();
            
            if (!em.contains(entity)) {
                entity = em.merge(entity);
            }
            em.remove(entity);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace(); // Mantido para debug no console
            
            throw e; //
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public T findById(ID id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(entityClass, id);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    @Override
    public List<T> findAll() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<T> cq = cb.createQuery(entityClass);
            cq.from(entityClass); 
            return em.createQuery(cq).getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}