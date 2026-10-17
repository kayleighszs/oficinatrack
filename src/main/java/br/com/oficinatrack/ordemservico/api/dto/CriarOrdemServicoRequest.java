package br.com.oficinatrack.ordemservico.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CriarOrdemServicoRequest(
        @NotNull @Positive Long clienteId,
        @NotNull @Positive Long veiculoId) {
}
