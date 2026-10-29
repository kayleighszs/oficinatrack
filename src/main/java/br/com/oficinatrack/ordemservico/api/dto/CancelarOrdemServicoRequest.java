package br.com.oficinatrack.ordemservico.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CancelarOrdemServicoRequest(@NotBlank @Size(max = 500) String motivo) {
}
