package com.gerenciadortarefas.repository;

import java.io.Serializable; 
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.gerenciadortarefas.model.StatusTarefa;
import com.gerenciadortarefas.model.Tarefa;
import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.util.JPAUtil;



public class TarefaRepository extends AbstractRepository<Tarefa, Long> implements Serializable {

    private static final long serialVersionUID = 1L; 

    public List<Tarefa> findByUsuario(Usuario usuario) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Tarefa> query = em.createQuery(
                    "SELECT t FROM Tarefa t WHERE t.usuario = :usuario ORDER BY t.dataCriacao DESC", Tarefa.class);
            query.setParameter("usuario", usuario);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Tarefa> findByUsuarioAndStatus(Usuario usuario, StatusTarefa status) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Tarefa> query = em
                    .createQuery("SELECT t FROM Tarefa t WHERE t.usuario = :usuario AND t.status = :status "
                            + "ORDER BY t.dataCriacao DESC", Tarefa.class);
            query.setParameter("usuario", usuario);
            query.setParameter("status", status);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Tarefa> findByUsuarioAndPrioridade(Usuario usuario,
            com.gerenciadortarefas.model.PrioridadeTarefa prioridade) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            TypedQuery<Tarefa> query = em
                    .createQuery("SELECT t FROM Tarefa t WHERE t.usuario = :usuario AND t.prioridade = :prioridade "
                            + "ORDER BY t.dataCriacao DESC", Tarefa.class);
            query.setParameter("usuario", usuario);
            query.setParameter("prioridade", prioridade);
            return query.getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Tarefa> findByResponsavel(Usuario responsavel) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT t FROM Tarefa t WHERE t.responsavel = :responsavel", Tarefa.class)
                    .setParameter("responsavel", responsavel).getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Tarefa> findByUsuarioAndResponsavel(Usuario usuario, Usuario responsavel) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em
                    .createQuery("SELECT t FROM Tarefa t WHERE t.usuario = :usuario AND t.responsavel = :responsavel",
                            Tarefa.class)
                    .setParameter("usuario", usuario).setParameter("responsavel", responsavel).getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}