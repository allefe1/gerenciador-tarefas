package com.gerenciadortarefas.repository;

import java.io.Serializable;
import java.util.List;

public interface Repository<T, ID extends Serializable> {
    
    T save(T entity);
    
    void remove(T entity);
    
    T findById(ID id);
    
    List<T> findAll();
}
