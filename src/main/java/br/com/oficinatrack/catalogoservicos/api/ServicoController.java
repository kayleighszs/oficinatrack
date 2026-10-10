package br.com.oficinatrack.catalogoservicos.api;

import br.com.oficinatrack.catalogoservicos.api.dto.in.ServicoRequestIn;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import br.com.oficinatrack.catalogoservicos.application.ServicoService;

@RestController
@RequestMapping("/servicos")
@Validated
public class ServicoController implements SwaggerServico {
    private final ServicoService servicoService;

    public ServicoController(ServicoService servicoService) {
        this.servicoService = servicoService;
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody ServicoRequestIn request) throws Exception {
        return servicoService.cadastrar(request);
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String nome,
            @PageableDefault(size = 10, page = 0) Pageable pageable) throws Exception {

        return servicoService.listar(nome, pageable);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable("id") Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @PutMapping("{id}")
    public ResponseEntity<?> atualizar(@PathVariable("id") Long id,
                                       @RequestBody ServicoRequestIn request) {
        return servicoService.atualizar(id, request);
    }

    @PatchMapping("{id}/inativar")
    public ResponseEntity<?> inativar(@PathVariable("id") Long id) {
        return servicoService.inativar(id);
    }

}
