package br.com.oficinatrack.publico.api;

import br.com.oficinatrack.publico.api.dto.AcessoPublicoResponse;
import br.com.oficinatrack.publico.application.AcessoPublicoService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/ordens-servico/{ordemId}/acessos-publicos")
@Validated
@PreAuthorize("hasAnyRole('GESTOR', 'ATENDENTE')")
public class AcessoPublicoController {
    private final AcessoPublicoService service;

    public AcessoPublicoController(AcessoPublicoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<AcessoPublicoResponse> emitir(@PathVariable @Positive Long ordemId) {
        AcessoPublicoResponse response = service.emitir(ordemId);
        return ResponseEntity.created(URI.create("/ordens-servico/" + ordemId + "/acessos-publicos/"
                + response.acessoId())).body(response);
    }

    @DeleteMapping("/{acessoId}")
    public ResponseEntity<Void> revogar(@PathVariable @Positive Long ordemId,
                                        @PathVariable @Positive Long acessoId) {
        service.revogar(ordemId, acessoId);
        return ResponseEntity.noContent().build();
    }
}
