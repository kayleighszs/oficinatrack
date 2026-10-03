package br.com.oficinatrack.estoque.exceptions;

import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;

public class PecaNaoEncontradaException extends RecursoNaoEncontradoException {
    public PecaNaoEncontradaException(String message) {
        super(message);
    }
}
