package br.com.oficinatrack.ordemservico.api.dto;

import br.com.oficinatrack.ordemservico.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrdemServicoResumoResponse(Long id, String numeroOs, Long clienteId, Long veiculoId,
                                         StatusOrdemServico status, BigDecimal totalAtual,
                                         LocalDateTime criadoEm, LocalDateTime atualizadoEm) {
    public static OrdemServicoResumoResponse de(OrdemServico os) {
        BigDecimal total = os.getOrcamentos().stream().reduce((a, b) -> b).map(Orcamento::getTotal).orElse(null);
        return new OrdemServicoResumoResponse(os.getId(), os.getNumeroOs(), os.getClienteId(), os.getVeiculoId(),
                os.getStatus(), total, os.getCriadoEm(), os.getAtualizadoEm());
    }
}
