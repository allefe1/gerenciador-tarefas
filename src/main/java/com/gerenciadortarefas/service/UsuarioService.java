package com.gerenciadortarefas.service;

import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.repository.UsuarioRepository;
import com.gerenciadortarefas.util.PasswordUtil;

public class UsuarioService {
    
    private UsuarioRepository usuarioRepository;
    
    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
    }
    
    public Usuario cadastrar(Usuario usuario) {
        // Verifica se já existe usuário com o mesmo email
        if (usuarioRepository.existsWithEmail(usuario.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }
        
        // Criptografa a senha antes de salvar
        String senhaCriptografada = PasswordUtil.hashPassword(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
        
        return usuarioRepository.save(usuario);
    }
    
    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        
        if (usuario == null) {
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
        // Se estiver atualizando a senha, criptografa novamente
        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            String senhaCriptografada = PasswordUtil.hashPassword(usuario.getSenha());
            usuario.setSenha(senhaCriptografada);
        } else {
            // Se não estiver atualizando a senha, recupera a senha atual
            Usuario usuarioAtual = usuarioRepository.findById(usuario.getId());
            usuario.setSenha(usuarioAtual.getSenha());
        }
        
        return usuarioRepository.save(usuario);
    }
}
