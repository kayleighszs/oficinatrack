package br.com.oficinatrack.shared.exception;

public class CredencialPublicaException extends RuntimeException {
    public CredencialPublicaException() {
        super("Credencial pública ausente ou inválida.");
    }
}
