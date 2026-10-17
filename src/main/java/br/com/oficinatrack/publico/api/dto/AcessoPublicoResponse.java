package br.com.oficinatrack.publico.api.dto;

import java.time.LocalDateTime;

public record AcessoPublicoResponse(Long acessoId, String token, String numeroOs, LocalDateTime expiraEm) {
}
