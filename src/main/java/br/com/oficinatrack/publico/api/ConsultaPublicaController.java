package br.com.oficinatrack.publico.api;

import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoResponse;
import br.com.oficinatrack.publico.api.dto.ConsultaPublicaResponse.*;
import br.com.oficinatrack.publico.api.dto.DecisaoOrcamentoRequest;
import br.com.oficinatrack.publico.application.ConsultaPublicaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/publico/ordens-servico/{numeroOs}")
@Validated
public class ConsultaPublicaController {
    private final ConsultaPublicaService service;

    public ConsultaPublicaController(ConsultaPublicaService service) { this.service = service; }

    @GetMapping("/status")
    public ResponseEntity<Status> status(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
                                         @PathVariable String numeroOs) {
        return semCache(service.consultarStatus(token, numeroOs));
    }

    @GetMapping("/orcamentos/{orcamentoId}")
    public ResponseEntity<OrcamentoPublico> orcamento(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable String numeroOs, @PathVariable @Positive Long orcamentoId) {
        return semCache(service.consultarOrcamento(token, numeroOs, orcamentoId));
    }

    @PostMapping("/orcamentos/{orcamentoId}/decisoes")
    public ResponseEntity<OrdemServicoResponse> decidir(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String token,
            @PathVariable String numeroOs, @PathVariable @Positive Long orcamentoId,
            @Valid @RequestBody DecisaoOrcamentoRequest request) {
        return semCache(service.decidir(token, numeroOs, orcamentoId, request));
    }

    private <T> ResponseEntity<T> semCache(T body) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }
}
