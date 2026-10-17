package br.com.oficinatrack.estoque.exceptions;

import br.com.oficinatrack.shared.exception.ConflitoException;

public class EstoqueInsuficienteExcepetion extends ConflitoException {
    public EstoqueInsuficienteExcepetion(String message) {
        super(message);
    }
}
