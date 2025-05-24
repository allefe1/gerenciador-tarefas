package com.gerenciadortarefas.controller;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private static final Logger LOGGER = Logger.getLogger(LoginBean.class.getName());

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
            Usuario usuarioAutenticado = usuarioService.autenticar(email, senha);

            
            
            if (usuarioAutenticado == null) {
                // Caso de segurança, se autenticar retornar null em vez de exceção para algum caso não previsto
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no login", "Usuário não encontrado ou dados inválidos."));
                this.senha = null;
                return null;
            }
            
            sessionBean.setUsuarioLogado(usuarioAutenticado);
            String nomeUsuario = (usuarioAutenticado.getNome() != null && !usuarioAutenticado.getNome().isEmpty())
                                 ? usuarioAutenticado.getNome()
                                 : usuarioAutenticado.getEmail();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Login efetuado!", "Bem-vindo(a), " + nomeUsuario + "!"));
            return "/tarefas?faces-redirect=true";

        } catch (RuntimeException re) {
            
            if ("Senha incorreta".equals(re.getMessage()) || (re.getCause() != null && "Senha incorreta".equals(re.getCause().getMessage())) ||
                "Usuário não encontrado".equals(re.getMessage()) || (re.getCause() != null && "Usuário não encontrado".equals(re.getCause().getMessage()))) {
                LOGGER.log(Level.WARNING, "Tentativa de login falhou para o email " + email + ": " + re.getMessage());
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Falha no login", "Email ou senha inválidos."));
            } else {
                // 
                LOGGER.log(Level.SEVERE, "Erro de RuntimeException inesperado durante o login para o email: " + email, re);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro no Login", "Ocorreu um problema durante a autenticação. Por favor, tente novamente."));
            }
            this.senha = null;
            return null;
        } catch (Exception e) {
            // Captura qualquer outra exceção genérica e inesperada
            LOGGER.log(Level.SEVERE, "Erro geral e inesperado durante o login para o email: " + email, e);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_FATAL, "Erro Crítico", "Ocorreu um problema sistêmico inesperado. Por favor, contate o suporte."));
            this.senha = null;
            return null;
        }
    }

    public String cadastrar() {
        try {
            usuarioService.cadastrar(novoUsuario);
            String nomeNovoUsuario = novoUsuario.getNome() != null && !novoUsuario.getNome().isEmpty()
                                     ? novoUsuario.getNome()
                                     : novoUsuario.getEmail();

            // Adicionando a mensagem ao escopo Flash para sobreviver ao redirect
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().getFlash().setKeepMessages(true); // diz ao JSF para manter as mensagens para a próxima view.
            facesContext.addMessage(null, // Mensagem global
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Cadastro Realizado!", "Usuário '" + nomeNovoUsuario + "' cadastrado com sucesso! Você já pode fazer o login."));

            novoUsuario = new Usuario(); 
            return "login?faces-redirect=true"; 

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao tentar cadastrar usuário: " + (novoUsuario != null ? novoUsuario.getEmail() : "novoUsuario era null"), e);
            String mensagemErroDetalhada = e.getMessage() != null && !e.getMessage().trim().isEmpty() ? e.getMessage() : "Não foi possível concluir o cadastro.";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro no Cadastro", mensagemErroDetalhada));
            
            
            return null;
        }
    }

    public String prepararCadastro() {
        this.novoUsuario = new Usuario();
        return "cadastro?faces-redirect=true"; 
    }

    
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