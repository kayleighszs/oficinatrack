package br.com.oficinatrack.ordemservico.api;

import br.com.oficinatrack.ordemservico.api.dto.*;
import br.com.oficinatrack.ordemservico.application.OrdemServicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.net.URI;

@RestController
@RequestMapping("/ordens-servico")
@Validated
public class OrdemServicoController {
    private final OrdemServicoService service;

    public OrdemServicoController(OrdemServicoService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTOR', 'ATENDENTE')")
    public ResponseEntity<OrdemServicoResponse> criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        OrdemServicoResponse response = service.criar(request);
        return ResponseEntity.created(URI.create("/ordens-servico/" + response.id())).body(response);
    }

    @PostMapping("/{id}/diagnosticos")
    @PreAuthorize("hasAnyRole('GESTOR', 'MECANICO')")
    public OrdemServicoResponse diagnosticar(@PathVariable @Positive Long id) { return service.iniciarDiagnostico(id); }

    @PostMapping("/{id}/itens-servico")
    @PreAuthorize("hasAnyRole('GESTOR', 'MECANICO')")
    public OrdemServicoResponse adicionarServico(@PathVariable @Positive Long id,
                                                 @Valid @RequestBody AdicionarItemRequest request) {
        return service.adicionarServico(id, request);
    }

    @PostMapping("/{id}/itens-peca")
    @PreAuthorize("hasAnyRole('GESTOR', 'MECANICO')")
    public OrdemServicoResponse adicionarPeca(@PathVariable @Positive Long id,
                                              @Valid @RequestBody AdicionarItemRequest request) {
        return service.adicionarPeca(id, request);
    }

    @PostMapping("/{id}/orcamentos")
    @PreAuthorize("hasAnyRole('GESTOR', 'ATENDENTE')")
    public OrdemServicoResponse calcular(@PathVariable @Positive Long id) { return service.calcularOrcamento(id); }

    @PostMapping("/{id}/orcamentos/{orcamentoId}/envios")
    @PreAuthorize("hasAnyRole('GESTOR', 'ATENDENTE')")
    public OrdemServicoResponse enviar(@PathVariable @Positive Long id,
                                       @PathVariable @Positive Long orcamentoId) {
        return service.enviarOrcamento(id, orcamentoId);
    }

    @PostMapping("/{id}/finalizacoes")
    @PreAuthorize("hasAnyRole('GESTOR', 'MECANICO')")
    public OrdemServicoResponse finalizar(@PathVariable @Positive Long id) { return service.finalizar(id); }

    @PostMapping("/{id}/entregas")
    @PreAuthorize("hasAnyRole('GESTOR', 'ATENDENTE')")
    public OrdemServicoResponse entregar(@PathVariable @Positive Long id) { return service.entregar(id); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTOR', 'ATENDENTE', 'MECANICO')")
    public OrdemServicoResponse detalhar(@PathVariable @Positive Long id) { return service.detalhar(id); }

    @GetMapping("/{id}/historico-status")
    @PreAuthorize("hasAnyRole('GESTOR', 'ATENDENTE', 'MECANICO')")
    public Object historico(@PathVariable @Positive Long id) { return service.detalhar(id).historico(); }
}
