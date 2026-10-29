package br.com.oficinatrack.ordemservico.application.port;

import java.math.BigDecimal;

public record ItemOrcavel(Long referenciaId, String descricao, BigDecimal valorUnitario) {
}
