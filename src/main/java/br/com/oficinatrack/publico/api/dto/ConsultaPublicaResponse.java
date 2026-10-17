package br.com.oficinatrack.publico.api.dto;

import br.com.oficinatrack.ordemservico.domain.*;
import java.math.BigDecimal;
import java.util.List;

public final class ConsultaPublicaResponse {
    private ConsultaPublicaResponse() {}

    public record Status(String numeroOs, StatusOrdemServico status, Long orcamentoAtualId) {}

    public record OrcamentoPublico(Long id, Integer versao, BigDecimal total, StatusOrcamento status,
                                   List<Item> servicos, List<Item> pecas) {}

    public record Item(String descricao, BigDecimal valorUnitario, Integer quantidade, BigDecimal subtotal) {}
}
