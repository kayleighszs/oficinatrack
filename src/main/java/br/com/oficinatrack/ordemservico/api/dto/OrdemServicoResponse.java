package br.com.oficinatrack.ordemservico.api.dto;

import br.com.oficinatrack.ordemservico.domain.ItemPecaOs;
import br.com.oficinatrack.ordemservico.domain.ItemServicoOs;
import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import br.com.oficinatrack.ordemservico.domain.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponse(
        Long id,
        Long clienteId,
        Long veiculoId,
        StatusOrdemServico status,
        BigDecimal valorTotal,
        List<ItemServicoResponse> itensServico,
        List<ItemPecaResponse> itensPeca,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {

    public static OrdemServicoResponse de(OrdemServico os) {
        return new OrdemServicoResponse(
                os.getId(),
                os.getClienteId(),
                os.getVeiculoId(),
                os.getStatus(),
                os.getValorTotal(),
                os.getItensServico().stream().map(ItemServicoResponse::de).toList(),
                os.getItensPeca().stream().map(ItemPecaResponse::de).toList(),
                os.getCriadoEm(),
                os.getAtualizadoEm());
    }

    public record ItemServicoResponse(Long id, Long servicoId, String descricao,
                                      BigDecimal valorUnitario, Integer quantidade, BigDecimal subtotal) {
        static ItemServicoResponse de(ItemServicoOs i) {
            return new ItemServicoResponse(i.getId(), i.getServicoId(), i.getDescricaoServico(),
                    i.getValorUnitario(), i.getQuantidade(), i.subtotal());
        }
    }

    public record ItemPecaResponse(Long id, Long pecaId, String descricao,
                                   BigDecimal valorUnitario, Integer quantidade, BigDecimal subtotal) {
        static ItemPecaResponse de(ItemPecaOs i) {
            return new ItemPecaResponse(i.getId(), i.getPecaId(), i.getDescricaoPeca(),
                    i.getValorUnitario(), i.getQuantidade(), i.subtotal());
        }
    }
}
