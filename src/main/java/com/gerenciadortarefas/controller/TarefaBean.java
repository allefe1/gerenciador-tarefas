package com.gerenciadortarefas.controller;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import com.gerenciadortarefas.model.PrioridadeTarefa;
import com.gerenciadortarefas.model.StatusTarefa;
import com.gerenciadortarefas.model.Tarefa;
import com.gerenciadortarefas.service.TarefaService;
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

    @ManagedProperty("#{sessionBean}")
    private SessionBean sessionBean;
    
    private TarefaService tarefaService;
    
    public TarefaBean() {
        this.tarefaService = new TarefaService();
        this.tarefa = new Tarefa();
    }
    
    @PostConstruct
    public void init() {
        if (sessionBean != null && sessionBean.isLogado()) {
            carregarTarefas();
        }
    }
    
    public String carregarTarefas() {
        System.out.println("Método carregarTarefas() iniciado");
        
        if (sessionBean == null || sessionBean.getUsuarioLogado() == null) {
            System.out.println("ERRO: sessionBean ou usuário logado é nulo");
            return null;
        }
        
        System.out.println("Carregando tarefas para usuário ID: " + sessionBean.getUsuarioLogado().getId());
        
        if (filtroStatus != null) {
            tarefas = tarefaService.filtrarPorStatus(sessionBean.getUsuarioLogado(), filtroStatus);
        } else if (filtroPrioridade != null) {
            tarefas = tarefaService.filtrarPorPrioridade(sessionBean.getUsuarioLogado(), filtroPrioridade);
        } else {
            tarefas = tarefaService.listarTarefasDoUsuario(sessionBean.getUsuarioLogado());
        }
        
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
        // Se a prioridade for null, limpa o filtro de status também
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
        
        // Se estiver fechando o formulário, limpe os dados da tarefa
        if (!mostrarFormulario) {
            this.tarefa = new Tarefa();
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
            
            // Usar EntityManager diretamente para salvar
            EntityManager em = JPAUtil.getEntityManager();
            EntityTransaction tx = em.getTransaction();
            
            try {
                tx.begin();
                System.out.println("Transação iniciada");
                
                // Verifica se é uma nova tarefa ou atualização
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
            this.mostrarFormulario = false; // Oculta o formulário após salvar
            carregarTarefas();
            
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tarefa salva com sucesso!"));
        } catch (Exception e) {
            System.out.println("ERRO geral ao salvar tarefa: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
        return null;
    }
    
    public String testarSalvar() {
        System.out.println("MÉTODO DE TESTE CHAMADO");
        FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Teste", "Método de teste chamado!"));
        return null;
    }
    
    // Métodos para edição - separados para funcionar com f:setPropertyActionListener
    public void setTarefaParaEditar(Tarefa tarefa) {
        this.tarefa = tarefa;
        this.mostrarFormulario = true; // Mostra o formulário ao editar
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
    
    // Métodos para concluir
    public void setTarefaParaConcluir(Long id) {
        this.tarefaSelecionadaId = id;
    }
    
    public String concluir() {
        try {
            tarefaService.concluirTarefa(tarefaSelecionadaId);
            carregarTarefas();
            
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tarefa concluída com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
        return null;
    }
    
    // Métodos para reabrir
    public void setTarefaParaReabrir(Long id) {
        this.tarefaSelecionadaId = id;
    }
    
    public String reabrir() {
        try {
            tarefaService.reabrirTarefa(tarefaSelecionadaId);
            carregarTarefas();
            
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Tarefa reaberta com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
        }
        return null;
    }
    
    // Getters e Setters
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
        
        // Se o status for null, limpa o filtro de prioridade também
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
        
        // Se a prioridade for null, limpa o filtro de status também
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
    
    // Métodos para obter os valores dos enums para os selects
    public StatusTarefa[] getStatusValues() {
        return StatusTarefa.values();
    }
    
    public PrioridadeTarefa[] getPrioridadeValues() {
        return PrioridadeTarefa.values();
    }
}
