package br.com.oficinatrack.ordemservico.api;

import br.com.oficinatrack.ordemservico.api.dto.in.CadastrarOrdemServicoIn;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import br.com.oficinatrack.ordemservico.application.OrdemServicoService;

@RestController
@RequestMapping("/servicos")
@Validated
public class OrdemServicoController {
    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(OrdemServicoService ordemServicoService) {
        this.ordemServicoService = ordemServicoService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@Valid @RequestBody CadastrarOrdemServicoIn request) throws Exception {
        return ordemServicoService.cadastrar(request);
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, page = 0) Pageable pageable) throws Exception {

        return ordemServicoService.listar(nome, pageable);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return ordemServicoService.buscarPorId(id);
    }

    @PutMapping("{id}")
    public ResponseEntity<?> atualizar(@PathVariable("id") Long id,
                                                     @RequestBody CadastrarOrdemServicoIn request) {
        return ordemServicoService.atualizar(id, request);
    }

    @PatchMapping("{id}/inativar")
    public ResponseEntity<?> inativar(@PathVariable("id") Long id) {
        return ordemServicoService.inativar(id);
    }

}
