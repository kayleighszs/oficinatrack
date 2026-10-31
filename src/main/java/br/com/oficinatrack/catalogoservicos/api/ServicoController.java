package br.com.oficinatrack.catalogoservicos.api;

import br.com.oficinatrack.catalogoservicos.api.dto.in.ServicoRequestIn;
import br.com.oficinatrack.catalogoservicos.api.dto.out.ServicoRequestOut;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import br.com.oficinatrack.catalogoservicos.application.ServicoService;

import java.util.Map;

@PreAuthorize("hasAnyRole('GESTOR', 'ATENDENTE', 'MECANICO')")
@RestController
@RequestMapping("/servicos")
@Validated
public class ServicoController {
    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public ResponseEntity<ServicoRequestOut> cadastrar(@Valid @RequestBody ServicoRequestIn request) {
        return servicoService.cadastrar(request);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listar(
            @RequestParam(required = false) String nome,
            @ParameterObject @PageableDefault(size = 10, page = 0) Pageable pageable) {

        return servicoService.listar(nome, pageable);
    }

    @GetMapping("{id}")
    public ResponseEntity<ServicoRequestOut> buscarPorId(@PathVariable("id") Long id) {
        return servicoService.buscarPorId(id);
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> atualizar(@PathVariable("id") Long id,
                                                     @RequestBody ServicoRequestIn request) {
        return servicoService.atualizar(id, request);
    }

    @PatchMapping("{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable("id") Long id) {
        return servicoService.inativar(id);
    }

}
