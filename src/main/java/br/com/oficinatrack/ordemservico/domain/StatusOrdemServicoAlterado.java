package br.com.oficinatrack.ordemservico.domain;

import java.time.LocalDateTime;

public record StatusOrdemServicoAlterado(
        Long ordemServicoId,
        StatusOrdemServico anterior,
        StatusOrdemServico novo,
        LocalDateTime ocorridoEm) {
}
