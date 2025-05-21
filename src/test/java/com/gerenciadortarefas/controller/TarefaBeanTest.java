package com.gerenciadortarefas.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gerenciadortarefas.model.Tarefa;
import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.service.TarefaService;
import com.gerenciadortarefas.util.FacesContextWrapper;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TarefaBeanTest {
    
    @Mock
    private FacesContextWrapper facesContextWrapper;
    
    @Mock
    private FacesContext facesContext;
    
    @Mock
    private ExternalContext externalContext;
    
    @Mock
    private TarefaService tarefaService;
    
    private TarefaBean tarefaBean;
    private SessionBean sessionBean;
    private Usuario usuarioTeste;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Configura o mock do FacesContextWrapper
        when(facesContextWrapper.getCurrentInstance()).thenReturn(facesContext);
        when(facesContext.getExternalContext()).thenReturn(externalContext);
        
        Map<String, Object> sessionMap = new HashMap<>();
        when(externalContext.getSessionMap()).thenReturn(sessionMap);
        
        // Cria um usuário de teste
        usuarioTeste = new Usuario();
        usuarioTeste.setId(1L);
        usuarioTeste.setNome("Usuário Teste");
        
        // Configura o SessionBean
        sessionBean = new SessionBean();
        sessionBean.setUsuarioLogado(usuarioTeste);
        
        // Configura o TarefaBean
        tarefaBean = new TarefaBean();
        tarefaBean.setSessionBean(sessionBean);
        
        // Substitui o tarefaService e facesContextWrapper por mocks
        setField(tarefaBean, "tarefaService", tarefaService);
        setField(tarefaBean, "facesContextWrapper", facesContextWrapper);
    }
    
    // Não precisamos mais do tearDown que usava FacesContextMocker
    
    // Utilitário para definir campos privados via reflexão
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testSalvarTarefa() {
        // Configura a tarefa
        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo("Nova Tarefa");
        tarefaBean.setTarefa(tarefa);
        
        // Configura o comportamento do mock
        when(tarefaService.salvar(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        // Executa o método a ser testado
        tarefaBean.salvar();
        
        // Verifica se o método do service foi chamado
        verify(tarefaService).salvar(any(Tarefa.class));
        
        // Verifica se a tarefa foi associada ao usuário correto
        assertEquals(usuarioTeste, tarefa.getUsuario());
        
        // Verifica se a mensagem foi adicionada
        verify(facesContext).addMessage(eq(null), any());
    }
    
    @Test
    public void testCarregarTarefas() {
        // Configura o comportamento do mock
        List<Tarefa> tarefasMock = new ArrayList<>();
        tarefasMock.add(new Tarefa());
        tarefasMock.add(new Tarefa());
        
        when(tarefaService.listarTarefasDoUsuario(usuarioTeste)).thenReturn(tarefasMock);
        
        // Executa o método a ser testado
        tarefaBean.carregarTarefas();
        
        // Verifica se o método do service foi chamado
        verify(tarefaService).listarTarefasDoUsuario(usuarioTeste);
        
        // Verifica se as tarefas foram carregadas corretamente
        assertEquals(2, tarefaBean.getTarefas().size());
    }
}
