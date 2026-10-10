package br.com.oficinatrack.estoque.api;

import br.com.oficinatrack.estoque.application.dto.MovimentacaoEstoqueRequest;
import br.com.oficinatrack.estoque.application.dto.PecaRequest;
import br.com.oficinatrack.estoque.application.dto.PecaResponse;
import br.com.oficinatrack.estoque.application.dto.PecaUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Peças e Estoque")
public interface SwaggerPeca {

    @PostMapping
    @Operation(
            summary = "Cadastra uma nova peça",
            description = "Cadastra uma peça com valor unitário, quantidade inicial em estoque e estoque mínimo."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Peça cadastrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PecaResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<PecaResponse> cadastrar(@Valid PecaRequest pecaRequest);

    @GetMapping
    @Operation(
            summary = "Lista peças",
            description = "Lista peças de forma paginada, com filtro opcional por nome e por estoque baixo "
                    + "(quantidade em estoque abaixo ou igual ao estoque mínimo). "
                    + "Parâmetros de paginação: page, size e sort."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de peças (content, totalElements, totalPages...)"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos")
    })
    ResponseEntity<Page<PecaResponse>> listar(
            @Parameter(description = "Filtra por parte do nome da peça") String nome,
            @Parameter(description = "Quando true, lista apenas peças com estoque baixo") Boolean estoqueBaixo,
            @ParameterObject Pageable pageable);

    @GetMapping("/{id}")
    @Operation(
            summary = "Busca peça por ID",
            description = "Retorna os dados de uma peça a partir do seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Peça encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PecaResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada")
    })
    ResponseEntity<PecaResponse> buscarPorId(
            @PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id);

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualiza dados cadastrais da peça",
            description = "Atualiza nome, valor unitário e estoque mínimo. A quantidade em estoque não é alterada "
                    + "aqui; use a movimentação de estoque."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Peça atualizada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PecaResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada")
    })
    ResponseEntity<PecaResponse> atualizar(
            @PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id,
            @Valid PecaUpdateRequest pecaRequest);

    @PatchMapping("/{id}/estoque")
    @Operation(
            summary = "Movimenta o estoque da peça",
            description = "Registra uma movimentação manual de ENTRADA ou SAIDA. A saída não pode deixar o estoque "
                    + "negativo. Cada movimentação fica registrada para rastreabilidade."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estoque movimentado; retorna a peça com a quantidade atualizada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PecaResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou estoque insuficiente para a saída"),
            @ApiResponse(responseCode = "404", description = "Peça não encontrada")
    })
    ResponseEntity<PecaResponse> movimentarEstoque(
            @PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id,
            @Valid MovimentacaoEstoqueRequest movimentacaoEstoqueRequest);
}
