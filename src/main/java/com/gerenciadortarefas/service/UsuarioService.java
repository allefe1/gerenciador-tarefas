package com.gerenciadortarefas.service;

import java.io.Serializable; // Importe a interface Serializable
import java.util.List;
import com.gerenciadortarefas.model.Usuario;
import com.gerenciadortarefas.repository.UsuarioRepository;
import com.gerenciadortarefas.util.PasswordUtil;

public class UsuarioService implements Serializable { // Adicione "implements Serializable"
    
    private static final long serialVersionUID = 1L; // Adicione um serialVersionUID

    private UsuarioRepository usuarioRepository;
    
    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
    }
    
    public Usuario cadastrar(Usuario usuario) {
        // Verifica se já existe usuário com o mesmo email
        if (usuarioRepository.existsWithEmail(usuario.getEmail())) {
            throw new RuntimeException("Email já cadastrado. Por favor, utilize outro email.");
        }
        
        // Validação de senha (exemplo: não pode ser nula ou vazia antes de criptografar)
        if (usuario.getSenha() == null || usuario.getSenha().trim().isEmpty()) {
            throw new RuntimeException("A senha não pode ser vazia.");
        }
        // Você também pode adicionar validações de força de senha aqui, se desejar.

        // Criptografa a senha antes de salvar
        String senhaCriptografada = PasswordUtil.hashPassword(usuario.getSenha());
        usuario.setSenha(senhaCriptografada);
        
        return usuarioRepository.save(usuario);
    }
    
    public Usuario autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        
        if (usuario == null) {
            // Não especifique se é o usuário ou a senha que está errada por segurança
            // A mensagem "Usuário não encontrado" pode ser um problema de segurança (enumeração de usuários)
            // É melhor uma mensagem genérica como "Email ou senha inválidos." que o LoginBean já trata.
            // Mas, para manter a lógica atual do seu LoginBean que verifica a mensagem:
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

        // Se uma nova senha foi fornecida (não nula e não vazia), criptografa e atualiza
        if (usuario.getSenha() != null && !usuario.getSenha().trim().isEmpty()) {
            // Validação de força da nova senha pode ser adicionada aqui
            String senhaCriptografada = PasswordUtil.hashPassword(usuario.getSenha());
            usuario.setSenha(senhaCriptografada);
        } else {
            // Se nenhuma nova senha foi fornecida, mantém a senha antiga do banco
            usuario.setSenha(usuarioExistente.getSenha());
        }
        
        // Atualiza outros campos (nome, email, etc., se necessário, com validações)
        // Exemplo: se o email for alterado, verificar se o novo email já existe para outro usuário
        if (usuario.getEmail() != null && !usuario.getEmail().equals(usuarioExistente.getEmail())) {
            if (usuarioRepository.existsWithEmail(usuario.getEmail())) {
                throw new RuntimeException("O novo email '" + usuario.getEmail() + "' já está cadastrado para outro usuário.");
            }
        }
        
        return usuarioRepository.save(usuario); // O método save do repositório deve lidar com merge/update
    }
    
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }
}