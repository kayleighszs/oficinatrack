package br.com.oficinatrack.veiculo.api;

import br.com.oficinatrack.veiculo.api.dto.VeiculoRequest;
import br.com.oficinatrack.veiculo.api.dto.VeiculoResponse;
import br.com.oficinatrack.veiculo.api.dto.VeiculoUpdateRequest;
import br.com.oficinatrack.veiculo.application.VeiculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> cadastrar(@Valid @RequestBody VeiculoRequest request) {
        VeiculoResponse response = veiculoService.cadastrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public List<VeiculoResponse> listar(@RequestParam(required = false) Long clienteId,
                                        @RequestParam(required = false) Boolean ativo) {
        return veiculoService.listar(clienteId, ativo);
    }

    @GetMapping("/{id}")
    public VeiculoResponse detalhar(@PathVariable Long id) {
        return veiculoService.detalhar(id);
    }

    @PatchMapping("/{id}")
    public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoUpdateRequest request) {
        return veiculoService.atualizar(id, request);
    }
}
