package com.gerenciadortarefas.util;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.gerenciadortarefas.model.Usuario;

@FacesConverter(value = "usuarioConverter")
public class UsuarioConverter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            Long id = Long.valueOf(value);
            EntityManager em = JPAUtil.getEntityManager();
            try {
                Usuario usuario = em.find(Usuario.class, id);
                return usuario;
            } catch (NoResultException e) {
                return null;
            } finally {
                em.close();
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof Usuario) {
            Usuario usuario = (Usuario) value;
            return usuario.getId() != null ? usuario.getId().toString() : "";
        }
        
        return "";
    }
}