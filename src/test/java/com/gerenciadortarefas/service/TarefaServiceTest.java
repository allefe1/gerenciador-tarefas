package com.gerenciadortarefas.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import com.gerenciadortarefas.model.Tarefa;
import com.gerenciadortarefas.model.StatusTarefa;
import com.gerenciadortarefas.model.Usuario;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TarefaServiceTest {
    
    private TarefaService tarefaService;
    private Usuario usuarioTeste;
    
    @BeforeAll
    public void setUp() {
        tarefaService = new TarefaService();
        usuarioTeste = new Usuario();
        usuarioTeste.setId(1L);
        usuarioTeste.setNome("Usuário Teste");
        usuarioTeste.setEmail("teste@exemplo.com");
    }
    
    @Test
    public void testSalvarTarefa() {
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo("Tarefa de Teste");
        tarefa.setDescricao("Descrição da tarefa de teste");
        tarefa.setUsuario(usuarioTeste);
        
        Tarefa tarefaSalva = tarefaService.salvar(tarefa);
        
        assertNotNull(tarefaSalva.getId());
        assertEquals("Tarefa de Teste", tarefaSalva.getTitulo());
        assertEquals(StatusTarefa.PENDENTE, tarefaSalva.getStatus());
    }
    
    @Test
    public void testConcluirTarefa() {
        // Cria e salva uma tarefa
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo("Tarefa para Concluir");
        tarefa.setUsuario(usuarioTeste);
        Tarefa tarefaSalva = tarefaService.salvar(tarefa);
        
        // Conclui a tarefa
        tarefaService.concluirTarefa(tarefaSalva.getId());
        
        // Verifica se a tarefa foi concluída
        Tarefa tarefaConcluida = tarefaService.buscarPorId(tarefaSalva.getId());
        assertEquals(StatusTarefa.CONCLUIDA, tarefaConcluida.getStatus());
        assertNotNull(tarefaConcluida.getDataConclusao());
    }
}
