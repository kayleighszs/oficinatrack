package br.com.oficinatrack.estoque.api;

import br.com.oficinatrack.estoque.application.PecaService;
import br.com.oficinatrack.estoque.application.dto.MovimentacaoEstoqueRequest;
import br.com.oficinatrack.estoque.application.dto.PecaRequest;
import br.com.oficinatrack.estoque.application.dto.PecaResponse;
import br.com.oficinatrack.estoque.application.dto.PecaUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pecas")
public class PecaController {
    // endpoints

    private final PecaService pecaService;

    public PecaController(PecaService pecaService) {
        this.pecaService = pecaService;
    }

    @PostMapping
    public ResponseEntity<PecaResponse> cadastrar(@Valid @RequestBody PecaRequest pecaRequest){
        PecaResponse response = pecaService.cadastrar(pecaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<PecaResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Boolean estoqueBaixo,
            Pageable pageable
            ){
        Page<PecaResponse> response = pecaService.listar(nome, estoqueBaixo, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PecaResponse> buscarPorId(@PathVariable Long id){
        PecaResponse response = pecaService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PecaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody PecaUpdateRequest pecaRequest){
        PecaResponse response = pecaService.atualizar(id, pecaRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<PecaResponse> movimentarEstoque(@PathVariable Long id, @Valid @RequestBody MovimentacaoEstoqueRequest movimentacaoEstoqueRequest){
        PecaResponse response = pecaService.movimentarEstoque(id, movimentacaoEstoqueRequest);
        return ResponseEntity.ok(response);
    }

}
