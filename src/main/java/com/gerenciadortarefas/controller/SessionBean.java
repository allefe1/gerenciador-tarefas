package com.gerenciadortarefas.controller;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import com.gerenciadortarefas.model.Usuario;

@ManagedBean
@SessionScoped
public class SessionBean implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Usuario usuarioLogado;
    
    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
    
    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }
    
    public boolean isLogado() {
        return usuarioLogado != null;
    }
    
    public String logout() {
        usuarioLogado = null;
        
        // Invalida a sessão HTTP
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance()
                .getExternalContext().getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        return "/login?faces-redirect=true";
    }
}
