package br.com.oficinatrack.ordemservico.application.port;

public interface ClienteConsultaPort {

    boolean existeAtivo(Long clienteId);
}
