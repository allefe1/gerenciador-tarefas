package com.gerenciadortarefas.util;

import javax.faces.context.FacesContext;

public class FacesContextWrapper {
    public FacesContext getCurrentInstance() {
        return FacesContext.getCurrentInstance();
    }
}
