package br.com.oficinatrack.estoque.exceptions;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;

public class EstoqueInsuficienteExcepetion extends RegraDeNegocioException {
    public EstoqueInsuficienteExcepetion(String message) {
        super(message);
    }
}
