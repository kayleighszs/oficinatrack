package br.com.oficinatrack.ordemservico.domain;

public enum StatusOrdemServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE,
    CANCELADA;

    public boolean aceitaNovosItens() {
        return this == RECEBIDA || this == EM_DIAGNOSTICO;
    }
}
