package com.gerenciadortarefas.service;

import java.io.Serializable; 
import java.util.List;
import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.repository.UsuarioRepository;
import com.gerenciadortarefas.util.PasswordUtil;

public class UsuarioService implements Serializable { 
    
    private static final long serialVersionUID = 1L; 

    private UsuarioRepository usuarioRepository;
    
    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
    }
    
    public Usuario cadastrar(Usuario usuario) {
        
        if (usuarioRepository.existsWithEmail(usuario.getEmail())) {
            throw new RuntimeException("Email já cadastrado. Por favor, utilize outro email.");
        }
        
        // Validação de senha (exemplo: não pode ser nula ou vazia antes de criptografar)
        if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
            throw new RuntimeException("A senha não pode ser vazia.");
        }
        

        // Criptografa a senha antes de salvar
        String senhaCriptografada = PasswordUtil.hashPassword(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
        
        return usuarioRepository.save(usuario);
    }
    
    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        
        if (usuario == null) {
            // Não especifique se é o usuário ou a senha que está errada por segurança
            
            
            
            throw new RuntimeException("Usuário não encontrado"); 
        }
        
        if (!PasswordUtil.checkPassword(senha, usuario.getSenha())) {
            throw new RuntimeException("Senha incorreta");
        }
        
        return usuario;
    }
    
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    public Usuario atualizar(Usuario usuario) {
        // Primeiro, verifica se o usuário a ser atualizado realmente existe
        Usuario usuarioExistente = usuarioRepository.findById(usuario.getId());
        if (usuarioExistente == null) {
            throw new RuntimeException("Usuário com ID " + usuario.getId() + " não encontrado para atualização.");
        }

        
        if (usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty()) {
            // Validação de força da nova senha pode ser adicionada aqui
            String senhaCriptografada = PasswordUtil.hashPassword(usuario.getSenha());
            usuario.setSenha(senhaCriptografada);
        } else {
            
            usuario.setSenha(usuarioExistente.getSenha());
        }
        
        
        // Exemplo: se o email for alterado, verificar se o novo email já existe para outro usuário
        if (usuario.getEmail() != null && !usuario.getEmail().equals(usuarioExistente.getEmail())) {
            if (usuarioRepository.existsWithEmail(usuario.getEmail())) {
                throw new RuntimeException("O novo email '" + usuario.getEmail() + "' já está cadastrado para outro usuário.");
            }
        }
        
        return usuarioRepository.save(usuario); 
    }
    
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
}