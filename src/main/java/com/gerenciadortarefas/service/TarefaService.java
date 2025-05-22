package com.gerenciadortarefas.service;

import java.util.Date;
import java.util.List;

import com.gerenciadortarefas.model.PrioridadeTarefa;
import com.gerenciadortarefas.model.StatusTarefa;
import com.gerenciadortarefas.model.Tarefa;
import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.repository.TarefaRepository;

public class TarefaService {
    
    private TarefaRepository tarefaRepository;
    
    public TarefaService() {
        this.tarefaRepository = new TarefaRepository();
    }
    
    public Tarefa salvar(Tarefa tarefa) {
        if (tarefa.getTitulo() == null || tarefa.getTitulo().trim().isEmpty()) {
            throw new RuntimeException("O título da tarefa é obrigatório");
        }
        
        if (tarefa.getUsuario() == null || tarefa.getUsuario().getId() == null) {
            throw new RuntimeException("A tarefa deve estar associada a um usuário");
        }
        
        return tarefaRepository.save(tarefa);
    }
    
    public void remover(Tarefa tarefa) {
        if (tarefa == null || tarefa.getId() == null) {
            throw new RuntimeException("Tarefa inválida para exclusão");
        }
        tarefaRepository.remove(tarefa);
    }
    
    public Tarefa buscarPorId(Long id) {
        return tarefaRepository.findById(id);
    }
    
    public List<Tarefa> listarTarefasDoResponsavel(Usuario responsavel) {
        return tarefaRepository.findByResponsavel(responsavel);
    }

    public List<Tarefa> listarTarefasDoUsuarioEResponsavel(Usuario usuario, Usuario responsavel) {
        return tarefaRepository.findByUsuarioAndResponsavel(usuario, responsavel);
    }
    
    public List<Tarefa> listarTarefasDoUsuario(Usuario usuario) {
        return tarefaRepository.findByUsuario(usuario);
    }
    
    public List<Tarefa> filtrarPorStatus(Usuario usuario, StatusTarefa status) {
        return tarefaRepository.findByUsuarioAndStatus(usuario, status);
    }
    
    public List<Tarefa> filtrarPorPrioridade(Usuario usuario, PrioridadeTarefa prioridade) {
        return tarefaRepository.findByUsuarioAndPrioridade(usuario, prioridade);
    }
    
    public Tarefa concluirTarefa(Long tarefaId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId);
        
        if (tarefa == null) {
            throw new RuntimeException("Tarefa não encontrada");
        }
        
        tarefa.setStatus(StatusTarefa.CONCLUIDA);
        tarefa.setDataConclusao(new Date());
        
        return tarefaRepository.save(tarefa);
    }
    
    public Tarefa reabrirTarefa(Long tarefaId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId);
        
        if (tarefa == null) {
            throw new RuntimeException("Tarefa não encontrada");
        }
        
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setDataConclusao(null);
        
        return tarefaRepository.save(tarefa);
    }
}
