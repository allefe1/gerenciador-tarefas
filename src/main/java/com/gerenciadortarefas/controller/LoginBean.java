package com.gerenciadortarefas.controller;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.service.UsuarioService;

@ManagedBean
@ViewScoped
public class LoginBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String email;
    private String senha;
    private Usuario novoUsuario;
    
    @ManagedProperty("#{sessionBean}")
    private SessionBean sessionBean;
    
    private UsuarioService usuarioService;
    
    public LoginBean() {
        this.usuarioService = new UsuarioService();
        this.novoUsuario = new Usuario();
    }
    
    public String login() {
        try {
            Usuario usuario = usuarioService.autenticar(email, senha);
            sessionBean.setUsuarioLogado(usuario);
            return "/tarefas?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro de login", e.getMessage()));
            return null;
        }
    }
    
    public String cadastrar() {
        try {
            usuarioService.cadastrar(novoUsuario);
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Usuário cadastrado com sucesso!"));
            novoUsuario = new Usuario();
            return null; // Permanece na mesma página
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", e.getMessage()));
            return null;
        }
    }
    
    public String prepararCadastro() {
        this.novoUsuario = new Usuario();
        return null; // Permanece na mesma página, mas mostra o formulário de cadastro
    }

    // Getters e Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Usuario getNovoUsuario() {
        return novoUsuario;
    }

    public void setNovoUsuario(Usuario novoUsuario) {
        this.novoUsuario = novoUsuario;
    }

    public SessionBean getSessionBean() {
        return sessionBean;
    }

    public void setSessionBean(SessionBean sessionBean) {
        this.sessionBean = sessionBean;
    }
}
