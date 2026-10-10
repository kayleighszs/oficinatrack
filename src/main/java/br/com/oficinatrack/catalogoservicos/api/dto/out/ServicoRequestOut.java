package br.com.oficinatrack.catalogoservicos.api.dto.out;

import java.math.BigDecimal;

public record ServicoRequestOut(Long id,
                                String nome,
                                BigDecimal valor,
                                Integer tempoMedioEstimado,
                                Boolean ativo) {
}
