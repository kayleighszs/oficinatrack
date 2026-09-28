package br.com.oficinatrack.ordemservico.domain;

import java.time.LocalDateTime;

public record HistoricoStatusOrdemServico(
        StatusOrdemServico anterior,
        StatusOrdemServico novo,
        LocalDateTime dataHora) {
}
