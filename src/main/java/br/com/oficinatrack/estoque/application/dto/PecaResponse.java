package br.com.oficinatrack.estoque.application.dto;

import br.com.oficinatrack.estoque.domain.Peca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PecaResponse(
        Long id,
        String nome,
        BigDecimal valorUnitario,
        Integer quantidadeEstoque,
        Integer estoqueMinimo,
        Boolean estoqueBaixo,
        Boolean ativo
) {
    public static PecaResponse from(Peca peca) {
        return new PecaResponse(
                peca.getId(),
                peca.getNome(),
                peca.getValorUnitario(),
                peca.getQuantidadeEstoque(),
                peca.getEstoqueMinimo(),
                peca.isEstoqueBaixo(),
                peca.getAtivo()
        );
    }
}
