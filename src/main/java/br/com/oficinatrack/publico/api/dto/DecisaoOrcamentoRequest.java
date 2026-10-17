package br.com.oficinatrack.publico.api.dto;

import jakarta.validation.constraints.NotNull;

public record DecisaoOrcamentoRequest(@NotNull Decisao decisao, String motivo) {
    public enum Decisao { APROVADO, RECUSADO }
}
