package br.com.oficinatrack.ordemservico.api;

import br.com.oficinatrack.ordemservico.api.dto.ItemPecaRequest;
import br.com.oficinatrack.ordemservico.api.dto.ItemServicoRequest;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoRequest;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoResponse;
import br.com.oficinatrack.ordemservico.application.OrdemServicoService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/ordens-servico")
@Validated
public class OrdemServicoController implements SwaggerOrdemServico {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criar(@RequestBody OrdemServicoRequest request) {
        OrdemServicoResponse response = ordemServicoService.criar(request);
        return ResponseEntity.created(URI.create("/ordens-servico/" + response.id())).body(response);
    }

    @PostMapping("{id}/servicos")
    public ResponseEntity<OrdemServicoResponse> adicionarServico(@PathVariable("id") Long id,
                                                                @RequestBody ItemServicoRequest request) {
        return ResponseEntity.ok(ordemServicoService.adicionarServico(id, request));
    }

    @PostMapping("{id}/pecas")
    public ResponseEntity<OrdemServicoResponse> adicionarPeca(@PathVariable("id") Long id,
                                                             @RequestBody ItemPecaRequest request) {
        return ResponseEntity.ok(ordemServicoService.adicionarPeca(id, request));
    }

    @PostMapping("{id}/orcamento/enviar")
    public ResponseEntity<OrdemServicoResponse> enviarOrcamento(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ordemServicoService.enviarOrcamento(id));
    }
}
