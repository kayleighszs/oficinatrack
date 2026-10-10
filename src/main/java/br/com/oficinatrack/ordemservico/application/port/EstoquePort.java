package br.com.oficinatrack.ordemservico.application.port;

import java.math.BigDecimal;
import java.util.Optional;

public interface EstoquePort {

    Optional<PecaEstoque> consultar(Long pecaId);

    record PecaEstoque(Long id, String nome, BigDecimal valorUnitario, int quantidadeDisponivel, boolean ativa) {
    }
}
