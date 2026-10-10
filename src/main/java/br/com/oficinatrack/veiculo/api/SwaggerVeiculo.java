package br.com.oficinatrack.veiculo.api;

import br.com.oficinatrack.veiculo.api.dto.VeiculoRequest;
import br.com.oficinatrack.veiculo.api.dto.VeiculoResponse;
import br.com.oficinatrack.veiculo.api.dto.VeiculoUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Tag(name = "Veículos")
public interface SwaggerVeiculo {

    @PostMapping
    @Operation(
            summary = "Cadastra um novo veículo",
            description = "Cadastra um veículo vinculado a um cliente existente. A placa é normalizada "
                    + "(maiúscula, sem espaço ou hífen), deve seguir o padrão antigo ou Mercosul e não pode repetir."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Veículo cadastrado; o cabeçalho Location aponta para o recurso criado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VeiculoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, placa inválida ou já cadastrada"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    })
    ResponseEntity<VeiculoResponse> cadastrar(@Valid VeiculoRequest request);

    @GetMapping
    @Operation(
            summary = "Lista veículos",
            description = "Lista veículos, com filtros opcionais por cliente e por situação (ativo/inativo)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Veículos encontrados (lista vazia quando não há resultado)",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = VeiculoResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos")
    })
    List<VeiculoResponse> listar(
            @Parameter(description = "Filtra pelos veículos de um cliente") Long clienteId,
            @Parameter(description = "Filtra por situação: true (ativos) ou false (inativos)") Boolean ativo);

    @GetMapping("/{id}")
    @Operation(
            summary = "Detalha um veículo",
            description = "Retorna os dados de um veículo a partir do seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Veículo encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VeiculoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    VeiculoResponse detalhar(@PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id);

    @PatchMapping("/{id}")
    @Operation(
            summary = "Atualiza parcialmente um veículo",
            description = "Altera somente os campos enviados (não nulos). Enviar {\"ativo\": false} inativa o veículo; "
                    + "não há exclusão física."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Veículo atualizado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VeiculoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, placa inválida ou já cadastrada"),
            @ApiResponse(responseCode = "404", description = "Veículo não encontrado")
    })
    VeiculoResponse atualizar(@PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id,
                              @Valid VeiculoUpdateRequest request);
}
