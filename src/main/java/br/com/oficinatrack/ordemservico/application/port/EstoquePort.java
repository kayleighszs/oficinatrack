package br.com.oficinatrack.ordemservico.application.port;

import java.util.List;

public interface EstoquePort {

    ItemOrcavel buscarPecaDisponivel(Long pecaId, int quantidade);

    void baixar(Long ordemServicoId, Long orcamentoId, List<ConsumoPeca> consumos);

    record ConsumoPeca(Long pecaId, int quantidade) {}
}
