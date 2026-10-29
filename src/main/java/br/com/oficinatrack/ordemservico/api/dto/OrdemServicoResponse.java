package br.com.oficinatrack.ordemservico.api.dto;

import br.com.oficinatrack.ordemservico.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponse(
        Long id,
        String numeroOs,
        Long clienteId,
        Long veiculoId,
        StatusOrdemServico status,
        List<ItemResponse> servicos,
        List<ItemResponse> pecas,
        List<OrcamentoResponse> orcamentos,
        List<HistoricoResponse> historico,
        String motivoCancelamento,
        LocalDateTime canceladoEm,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm) {

    public static OrdemServicoResponse de(OrdemServico os) {
        return new OrdemServicoResponse(
                os.getId(), os.getNumeroOs(), os.getClienteId(), os.getVeiculoId(), os.getStatus(),
                os.getItensServico().stream().map(i -> new ItemResponse(i.getId(), i.getServicoId(),
                        i.getDescricao(), i.getValorUnitario(), i.getQuantidade(), i.subtotal())).toList(),
                os.getItensPeca().stream().map(i -> new ItemResponse(i.getId(), i.getPecaId(),
                        i.getDescricao(), i.getValorUnitario(), i.getQuantidade(), i.subtotal())).toList(),
                os.getOrcamentos().stream().map(OrcamentoResponse::de).toList(),
                os.getHistorico().stream().map(HistoricoResponse::de).toList(),
                os.getMotivoCancelamento(), os.getCanceladoEm(),
                os.getCriadoEm(), os.getAtualizadoEm());
    }

    public record ItemResponse(Long id, Long referenciaId, String descricao, BigDecimal valorUnitario,
                               Integer quantidade, BigDecimal subtotal) {}

    public record OrcamentoResponse(Long id, Integer versao, BigDecimal total, StatusOrcamento status,
                                    LocalDateTime criadoEm, LocalDateTime enviadoEm,
                                    LocalDateTime decididoEm, String motivoRecusa) {
        static OrcamentoResponse de(Orcamento o) {
            return new OrcamentoResponse(o.getId(), o.getVersao(), o.getTotal(), o.getStatus(),
                    o.getCriadoEm(), o.getEnviadoEm(), o.getDecididoEm(), o.getMotivoRecusa());
        }
    }

    public record HistoricoResponse(Long id, StatusOrdemServico anterior, StatusOrdemServico novo,
                                    LocalDateTime alteradoEm) {
        static HistoricoResponse de(HistoricoStatusOrdemServico h) {
            return new HistoricoResponse(h.getId(), h.getStatusAnterior(), h.getStatusNovo(), h.getAlteradoEm());
        }
    }
}
