package br.com.oficinatrack.ordemservico.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class OrdemServicoRequest {

    @NotNull(message = "clienteId é obrigatório")
    @Positive(message = "clienteId deve ser maior que zero")
    private Long clienteId;

    @NotNull(message = "veiculoId é obrigatório")
    @Positive(message = "veiculoId deve ser maior que zero")
    private Long veiculoId;

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public Long getVeiculoId() { return veiculoId; }
    public void setVeiculoId(Long veiculoId) { this.veiculoId = veiculoId; }
}
