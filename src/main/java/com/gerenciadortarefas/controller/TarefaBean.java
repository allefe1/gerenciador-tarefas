package com.gerenciadortarefas.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.gerenciadortarefas.model.PrioridadeTarefa;
import com.gerenciadortarefas.model.StatusTarefa;
import com.gerenciadortarefas.model.Tarefa;
import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.service.TarefaService;
import com.gerenciadortarefas.service.UsuarioService;
import com.gerenciadortarefas.util.FacesContextWrapper;
import com.gerenciadortarefas.util.JPAUtil;

@ManagedBean
@ViewScoped
public class TarefaBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Tarefa tarefa;
    private List<Tarefa> tarefas;
    private StatusTarefa filtroStatus;
    private PrioridadeTarefa filtroPrioridade;
    private Long tarefaSelecionadaId;
    private boolean mostrarFormulario = false;
    
    
    private List<Usuario> usuarios;
    private Usuario responsavelSelecionado;
    
    private FacesContextWrapper facesContextWrapper;

    @ManagedProperty("#{sessionBean}")
    private SessionBean sessionBean;
    
    private TarefaService tarefaService;
    private UsuarioService usuarioService;
    
    public TarefaBean() {
        this.tarefaService = new TarefaService();
        this.usuarioService = new UsuarioService();
        this.tarefa = new Tarefa();
        this.facesContextWrapper = new FacesContextWrapper();
    }
    
    @PostConstruct
    public void init() {
        if (sessionBean != null && sessionBean.isLogado()) {
            carregarTarefas();
            carregarUsuarios();
        }
    }
    
    // Novo método para carregar usuários para o seletor de responsável
    public void carregarUsuarios() {
        try {
            this.usuarios = usuarioService.listarTodos();
            System.out.println("Usuários carregados: " + (usuarios != null ? usuarios.size() : "null"));
        } catch (Exception e) {
            System.out.println("ERRO ao carregar usuários: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public String carregarTarefas() {
        System.out.println("Método carregarTarefas() iniciado");
        
        if (sessionBean == null || sessionBean.getUsuarioLogado() == null) {
            System.out.println("ERRO: sessionBean ou usuário logado é nulo");
            return null;
        }
        
        Usuario usuarioLogado = sessionBean.getUsuarioLogado();
        System.out.println("Carregando tarefas para usuário ID: " + usuarioLogado.getId());
        
        // Busca combinada de tarefas
        List<Tarefa> tarefasCombinadas = new ArrayList<>();
        
        // Tarefas onde o usuário é dono
        tarefasCombinadas.addAll(tarefaService.listarTarefasDoUsuario(usuarioLogado));
        
        // Tarefas onde o usuário é responsável
        tarefasCombinadas.addAll(tarefaService.listarTarefasDoResponsavel(usuarioLogado));
        
        // Remove duplicatas
        Set<Tarefa> tarefasUnicas = new LinkedHashSet<>(tarefasCombinadas);
        List<Tarefa> tarefasFiltradas = new ArrayList<>(tarefasUnicas);

        
        if (filtroStatus != null) {
            tarefasFiltradas = tarefasFiltradas.stream()
                .filter(t -> t.getStatus() == filtroStatus)
                .collect(Collectors.toList());
        } else if (filtroPrioridade != null) {
            tarefasFiltradas = tarefasFiltradas.stream()
                .filter(t -> t.getPrioridade() == filtroPrioridade)
                .collect(Collectors.toList());
        }

        this.tarefas = tarefasFiltradas;
        
        System.out.println("Tarefas carregadas: " + (tarefas != null ? tarefas.size() : "null"));
        return null;
    }
    
    public String filtrarPorStatus() {
        // Se o status for null, limpa o filtro de prioridade também
        if (filtroStatus == null) {
            filtroPrioridade = null;
        }
        carregarTarefas();
        return null;
    }
    
    public String filtrarPorPrioridade() {
        
        if (filtroPrioridade == null) {
            filtroStatus = null;
        }
        carregarTarefas();
        return null;
    }
    
    public String limparFiltros() {
        filtroStatus = null;
        filtroPrioridade = null;
        carregarTarefas();
        return null;
    }
    
    public String toggleFormulario() {
        this.mostrarFormulario = !this.mostrarFormulario;
        
        
        if (!mostrarFormulario) {
            this.tarefa = new Tarefa();
            this.responsavelSelecionado = null;
        }
        
        return null;
    }
    
    public String salvar() {
        System.out.println("Método salvar() iniciado");
        try {
            System.out.println("Salvando tarefa: " + tarefa.getTitulo());
            
            if (sessionBean == null) {
                System.out.println("ERRO: sessionBean é nulo");
                throw new RuntimeException("Sessão não encontrada");
            }
            
            if (sessionBean.getUsuarioLogado() == null) {
                System.out.println("ERRO: usuário logado é nulo");
                throw new RuntimeException("Usuário não está logado");
            }
            
            tarefa.setUsuario(sessionBean.getUsuarioLogado());
            
            // Definir o responsável antes de salvar
            if (responsavelSelecionado != null) {
                tarefa.setResponsavel(responsavelSelecionado);
                System.out.println("Definindo responsável: " + responsavelSelecionado.getNome() + " (ID: " + responsavelSelecionado.getId() + ")");
            }
            
            
            EntityManager em = JPAUtil.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            
            try {
                tx.begin();
                System.out.println("Transação iniciada");
                
                
                if (tarefa.getId() == null) {
                    System.out.println("Inserindo nova tarefa");
                    em.persist(tarefa);
                } else {
                    System.out.println("Atualizando tarefa existente");
                    tarefa = em.merge(tarefa);
                }
                
                tx.commit();
                System.out.println("Transação confirmada. Tarefa ID: " + tarefa.getId());
            } catch (Exception e) {
                System.out.println("ERRO ao salvar tarefa: " + e.getMessage());
                e.printStackTrace();
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                    System.out.println("Transação revertida");
                }
                throw e;
            } finally {
                em.close();
            }
            
            // Limpar o formulário e recarregar tarefas
            this.tarefa = new Tarefa();
            this.responsavelSelecionado = null;
            this.mostrarFormulario = false; // Oculta o formulário após salvar
            carregarTarefas();
            
            facesContextWrapper.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tarefa salva com sucesso!"));
        } catch (Exception e) {
            System.out.println("ERRO geral ao salvar tarefa: " + e.getMessage());
            e.printStackTrace();
            facesContextWrapper.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
        return null;
    }
    
    public String testarSalvar() {
        System.out.println("MÉTODO DE TESTE CHAMADO");
        facesContextWrapper.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Teste", "Método de teste chamado!"));
        return null;
    }
    
    
    public void setTarefaParaEditar(Tarefa tarefa) {
        this.tarefa = tarefa;
        this.responsavelSelecionado = tarefa.getResponsavel(); 
        this.mostrarFormulario = true; 
    }
    
    // Métodos para exclusão
    public void setTarefaParaExcluir(Long id) {
        this.tarefaSelecionadaId = id;
    }
    
    public String excluir() {
        try {
            if (tarefaSelecionadaId != null) {
                // Buscar a tarefa diretamente do banco de dados
                EntityManager em = JPAUtil.getEntityManager();
                EntityTransaction tx = em.getTransaction();
                
                try {
                    tx.begin();
                    Tarefa tarefa = em.find(Tarefa.class, tarefaSelecionadaId);
                    if (tarefa != null) {
                        em.remove(tarefa);
                        System.out.println("Tarefa removida: " + tarefa.getTitulo());
                    } else {
                        System.out.println("Tarefa não encontrada com ID: " + tarefaSelecionadaId);
                    }
                    tx.commit();
                } catch (Exception e) {
                    if (tx != null && tx.isActive()) {
                        tx.rollback();
                    }
                    e.printStackTrace();
                } finally {
                    em.close();
                }
                
                carregarTarefas();
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
    public void setTarefaParaConcluir(Long id) {
        this.tarefaSelecionadaId = id;
    }
    
    public String concluir() {
        try {
            tarefaService.concluirTarefa(tarefaSelecionadaId);
            carregarTarefas();
            
            facesContextWrapper.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tarefa concluída com sucesso!"));
        } catch (Exception e) {
            facesContextWrapper.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
        return null;
    }
    
    
    public void setTarefaParaReabrir(Long id) {
        this.tarefaSelecionadaId = id;
    }
    
    public String reabrir() {
        try {
            tarefaService.reabrirTarefa(tarefaSelecionadaId);
            carregarTarefas();
            
            facesContextWrapper.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tarefa reaberta com sucesso!"));
        } catch (Exception e) {
            facesContextWrapper.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
        return null;
    }
    
    
    public Tarefa getTarefa() {
        return tarefa;
    }

    public void setTarefa(Tarefa tarefa) {
        this.tarefa = tarefa;
    }

    public List<Tarefa> getTarefas() {
        return tarefas;
    }

    public void setTarefas(List<Tarefa> tarefas) {
        this.tarefas = tarefas;
    }

    public StatusTarefa getFiltroStatus() {
        return filtroStatus;
    }

    public void setFiltroStatus(StatusTarefa filtroStatus) {
        this.filtroStatus = filtroStatus;
        
        
        if (filtroStatus == null) {
            filtroPrioridade = null;
        }
        
        carregarTarefas();
    }

    public PrioridadeTarefa getFiltroPrioridade() {
        return filtroPrioridade;
    }

    public void setFiltroPrioridade(PrioridadeTarefa filtroPrioridade) {
        this.filtroPrioridade = filtroPrioridade;
        
        
        if (filtroPrioridade == null) {
            filtroStatus = null;
        }
        
        carregarTarefas();
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }
    
    public Long getTarefaSelecionadaId() {
        return tarefaSelecionadaId;
    }

    public void setTarefaSelecionadaId(Long tarefaSelecionadaId) {
        this.tarefaSelecionadaId = tarefaSelecionadaId;
    }
    
    public boolean isMostrarFormulario() {
        return mostrarFormulario;
    }
    
    public boolean getMostrarFormulario() {
        return mostrarFormulario;
    }

    public void setMostrarFormulario(boolean mostrarFormulario) {
        this.mostrarFormulario = mostrarFormulario;
    }
    
    public FacesContextWrapper getFacesContextWrapper() {
        return facesContextWrapper;
    }

    public void setFacesContextWrapper(FacesContextWrapper facesContextWrapper) {
        this.facesContextWrapper = facesContextWrapper;
    }
    
    // Novos getters e setters para o responsável
    public List<Usuario> getUsuarios() {
        return usuarios;
    }
    
    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarios = usuarios;
    }
    
    public Usuario getResponsavelSelecionado() {
        return responsavelSelecionado;
    }
    
    public void setResponsavelSelecionado(Usuario responsavelSelecionado) {
        // O responsável pode ser nulo (quando a opção "Selecione um responsável" é escolhida)
        this.responsavelSelecionado = responsavelSelecionado;
        System.out.println("Responsável selecionado: " + (responsavelSelecionado != null ? 
            responsavelSelecionado.getNome() + " (ID: " + responsavelSelecionado.getId() + ")" : "nenhum"));
    }
    
    // Métodos para obter os valores dos enums para os selects
    public StatusTarefa[] getStatusValues() {
        return StatusTarefa.values();
    }
    
    public PrioridadeTarefa[] getPrioridadeValues() {
        return PrioridadeTarefa.values();
    }
}