package br.com.oficinatrack.ordemservico.application.port;

import java.util.Optional;

public interface VeiculoConsultaPort {

    Optional<VeiculoInfo> buscar(Long veiculoId);

    record VeiculoInfo(Long id, Long clienteId, boolean ativo) {
    }
}
