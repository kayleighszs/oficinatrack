package br.com.oficinatrack.ordemservico.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AdicionarItemRequest(
        @NotNull @Positive Long referenciaId,
        @NotNull @Positive Integer quantidade) {
}
