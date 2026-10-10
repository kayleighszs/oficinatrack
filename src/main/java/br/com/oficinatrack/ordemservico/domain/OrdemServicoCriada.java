package br.com.oficinatrack.ordemservico.domain;

import java.time.LocalDateTime;

public record OrdemServicoCriada(Long ordemServicoId, Long clienteId, Long veiculoId, LocalDateTime criadaEm) {
}
