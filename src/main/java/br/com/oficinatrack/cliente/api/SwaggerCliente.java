package br.com.oficinatrack.cliente.api;

import br.com.oficinatrack.cliente.api.dto.AtualizarClienteRequest;
import br.com.oficinatrack.cliente.api.dto.CadastrarClienteRequest;
import br.com.oficinatrack.cliente.api.dto.ClienteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

public interface SwaggerCliente {

    @Operation(
            summary = "Cadastra um novo cliente",
            description = "Realiza o cadastro de um cliente com nome, CPF/CNPJ (a máscara é removida e o tipo de pessoa é definido automaticamente), telefone e e-mail opcionais. CPF/CNPJ duplicado retorna 400."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente cadastrado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos"
            )
    })
    @PostMapping
    ResponseEntity<Void> cadastrar(@Valid CadastrarClienteRequest request);

    @GetMapping
    @Operation(
            summary = "Lista clientes",
            description = "Consulta clientes ativos utilizando CPF/CNPJ (com ou sem máscara) e/ou nome como filtros."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Clientes encontrados",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ClienteResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Parâmetros de consulta inválidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Nenhum cliente encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<List<ClienteResponse>> listar(String cpfCnpj, String nome);

    @GetMapping("{id}")
    @Operation(
            summary = "Busca cliente por ID",
            description = "Retorna os dados de um cliente a partir do seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID inválido",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<ClienteResponse> buscarPorId(@PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id);

    @PutMapping("{id}")
    @Operation(
            summary = "Atualiza dados cadastrais do cliente",
            description = "Atualiza nome, telefone e e-mail do cliente já cadastrado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<ClienteResponse> atualizar(@PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id,
                                            @Valid AtualizarClienteRequest request);

    @PatchMapping("{id}/inativar")
    @Operation(
            summary = "Inativa um cliente",
            description = "Marca o cliente como inativo sem excluir o registro."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente inativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<ClienteResponse> inativar(@PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id);
}
