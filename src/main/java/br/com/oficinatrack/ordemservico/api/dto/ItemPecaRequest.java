package br.com.oficinatrack.ordemservico.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ItemPecaRequest {

    @NotNull(message = "pecaId é obrigatório")
    @Positive(message = "pecaId deve ser maior que zero")
    private Long pecaId;

    @NotNull(message = "quantidade é obrigatória")
    @Min(value = 1, message = "quantidade deve ser no mínimo 1")
    @Max(value = 9999, message = "quantidade deve ser no máximo 9999")
    private Integer quantidade;

    public Long getPecaId() { return pecaId; }
    public void setPecaId(Long pecaId) { this.pecaId = pecaId; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}
