package br.com.oficinatrack.ordemservico.api.dto.out;

import java.math.BigDecimal;

public record CadastrarOrdemServicoOut(Long id,
                                       String nome,
                                       BigDecimal valor,
                                       Integer tempoMedioEstimado) {
}
