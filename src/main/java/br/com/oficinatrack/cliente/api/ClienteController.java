package br.com.oficinatrack.cliente.api;

import br.com.oficinatrack.cliente.application.ClienteService;
import br.com.oficinatrack.cliente.api.dto.AtualizarClienteRequest;
import br.com.oficinatrack.cliente.api.dto.CadastrarClienteRequest;
import br.com.oficinatrack.cliente.api.dto.ClienteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
@Validated
public class ClienteController implements SwaggerCliente {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<Void> cadastrar(@RequestBody CadastrarClienteRequest request) {
        clienteService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(@RequestParam(value = "cpfCnpj", required = false) String cpfCnpj,
                                                        @RequestParam(value = "nome", required = false) String nome) {
        return ResponseEntity.ok(clienteService.listarClientes(cpfCnpj, nome));
    }

    @GetMapping("{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable("id") Long id) {
        ClienteResponse response = clienteService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("{id}")
    public ResponseEntity<ClienteResponse> atualizar(@PathVariable("id") Long id,
                                                    @RequestBody AtualizarClienteRequest request) {
        return ResponseEntity.ok(clienteService.atualizar(id, request));
    }

    @PatchMapping("{id}/inativar")
    public ResponseEntity<ClienteResponse> inativar(@PathVariable("id") Long id) {
        return ResponseEntity.ok(clienteService.inativar(id));
    }
}
