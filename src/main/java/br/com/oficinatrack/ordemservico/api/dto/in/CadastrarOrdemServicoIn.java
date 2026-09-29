package br.com.oficinatrack.ordemservico.api.dto.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CadastrarOrdemServicoIn(
        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser maior que zero.")
        BigDecimal valor,

        @NotNull(message = "O tempo médio estimado é obrigatório.")
        @Positive(message = "O tempo médio estimado deve ser maior que zero.")
        Integer tempoMedioEstimado
) {
}
