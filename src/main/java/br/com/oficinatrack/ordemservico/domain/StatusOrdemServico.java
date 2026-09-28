package br.com.oficinatrack.ordemservico.domain;

import java.util.EnumSet;
import java.util.Set;

public enum StatusOrdemServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE,
    ORCAMENTO_RECUSADO,
    CANCELADA;

    public Set<StatusOrdemServico> transicoesPermitidas() {
        return switch (this) {
            case RECEBIDA -> EnumSet.of(EM_DIAGNOSTICO, CANCELADA);
            case EM_DIAGNOSTICO -> EnumSet.of(AGUARDANDO_APROVACAO, CANCELADA);
            case AGUARDANDO_APROVACAO -> EnumSet.of(EM_EXECUCAO, ORCAMENTO_RECUSADO, CANCELADA);
            case ORCAMENTO_RECUSADO -> EnumSet.of(EM_DIAGNOSTICO, CANCELADA);
            case EM_EXECUCAO -> EnumSet.of(FINALIZADA);
            case FINALIZADA -> EnumSet.of(ENTREGUE);
            case ENTREGUE, CANCELADA -> EnumSet.noneOf(StatusOrdemServico.class);
        };
    }

    public boolean podeTransicionarPara(StatusOrdemServico destino) {
        return destino != null && transicoesPermitidas().contains(destino);
    }

    public boolean isTerminal() {
        return transicoesPermitidas().isEmpty();
    }
}
