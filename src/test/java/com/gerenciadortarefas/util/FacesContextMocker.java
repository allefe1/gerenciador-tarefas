package com.gerenciadortarefas.util;

import javax.faces.context.FacesContext;

public class FacesContextMocker {
    public static void setFacesContext(FacesContext facesContext) {
        try {
            java.lang.reflect.Field field = FacesContext.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, facesContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
