package br.com.oficinatrack.ordemservico.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ItemServicoRequest {

    @NotNull(message = "servicoId é obrigatório")
    @Positive(message = "servicoId deve ser maior que zero")
    private Long servicoId;

    @NotNull(message = "quantidade é obrigatória")
    @Min(value = 1, message = "quantidade deve ser no mínimo 1")
    @Max(value = 9999, message = "quantidade deve ser no máximo 9999")
    private Integer quantidade;

    public Long getServicoId() { return servicoId; }
    public void setServicoId(Long servicoId) { this.servicoId = servicoId; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
