package br.com.oficinatrack.ordemservico.application.port;

public interface ClienteVeiculoPort {

    void validarVinculo(Long clienteId, Long veiculoId);
}
