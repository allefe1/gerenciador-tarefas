package com.gerenciadortarefas.repository;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import com.gerenciadortarefas.model.Tarefa;
import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.util.TestJPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TarefaRepositoryTest {
    
    private TarefaRepository tarefaRepository;
    private Usuario usuarioTeste;
    
    @BeforeAll
    public void setUp() {
        tarefaRepository = new TarefaRepository();
        
        // Cria um usuário de teste
        usuarioTeste = new Usuario();
        usuarioTeste.setNome("Usuário Teste");
        usuarioTeste.setEmail("teste" + System.currentTimeMillis() + "@exemplo.com");
        usuarioTeste.setSenha("senha123");
        
        EntityManager em = TestJPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(usuarioTeste);
            tx.commit();
        } finally {
            em.close();
        }
    }
    
    @AfterAll
    public void tearDown() {
        // Limpa os dados de teste
        EntityManager em = TestJPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Tarefa t WHERE t.usuario.id = :userId")
              .setParameter("userId", usuarioTeste.getId())
              .executeUpdate();
            em.createQuery("DELETE FROM Usuario u WHERE u.id = :userId")
              .setParameter("userId", usuarioTeste.getId())
              .executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }
    }
    
    @Test
    public void testSalvarEBuscarTarefa() {
        // Cria uma nova tarefa
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo("Tarefa de Teste");
        tarefa.setDescricao("Descrição da tarefa de teste");
        tarefa.setUsuario(usuarioTeste);
        
        // Salva a tarefa
        Tarefa tarefaSalva = tarefaRepository.save(tarefa);
        assertNotNull(tarefaSalva.getId());
        
        // Busca a tarefa pelo ID
        Tarefa tarefaEncontrada = tarefaRepository.findById(tarefaSalva.getId());
        assertNotNull(tarefaEncontrada);
        assertEquals("Tarefa de Teste", tarefaEncontrada.getTitulo());
    }
    
    @Test
    public void testListarTarefasDoUsuario() {
        // Limpa tarefas existentes
        EntityManager em = TestJPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Tarefa t WHERE t.usuario.id = :userId")
              .setParameter("userId", usuarioTeste.getId())
              .executeUpdate();
            tx.commit();
        } finally {
            em.close();
        }
        
        // Cria 3 tarefas para o usuário
        for (int i = 1; i <= 3; i++) {
            Tarefa tarefa = new Tarefa();
            tarefa.setTitulo("Tarefa " + i);
            tarefa.setUsuario(usuarioTeste);
            tarefaRepository.save(tarefa);
        }
        
        // Lista as tarefas do usuário
        List<Tarefa> tarefas = tarefaRepository.findByUsuario(usuarioTeste);
        assertEquals(3, tarefas.size());
    }
}
