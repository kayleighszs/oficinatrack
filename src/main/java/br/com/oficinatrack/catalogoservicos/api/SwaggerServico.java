package br.com.oficinatrack.catalogoservicos.api;

import br.com.oficinatrack.catalogoservicos.api.dto.in.ServicoRequestIn;
import br.com.oficinatrack.catalogoservicos.api.dto.out.ServicoRequestOut;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "Catálogo de Serviços")
public interface SwaggerServico {

    @PostMapping
    @Operation(
            summary = "Cadastra um novo serviço",
            description = "Cadastra um serviço no catálogo com nome, valor e tempo médio estimado (em minutos). "
                    + "Todos os campos são obrigatórios e o valor deve ser maior que zero."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Serviço cadastrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicoRequestOut.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    ResponseEntity<?> cadastrar(@Valid ServicoRequestIn request) throws Exception;

    @GetMapping
    @Operation(
            summary = "Lista serviços",
            description = "Lista os serviços do catálogo de forma paginada, com filtro opcional por parte do nome. "
                    + "A resposta traz ordens (itens da página), currentPage, totalItens e totalPages. "
                    + "Parâmetros de paginação: page, size e sort."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de serviços"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de consulta inválidos")
    })
    ResponseEntity<?> listar(
            @Parameter(description = "Filtra por parte do nome do serviço") String nome,
            @ParameterObject Pageable pageable) throws Exception;

    @GetMapping("{id}")
    @Operation(
            summary = "Busca serviço por ID",
            description = "Retorna os dados de um serviço do catálogo a partir do seu identificador."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Serviço encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ServicoRequestOut.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    ResponseEntity<?> buscarPorId(@PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id);

    @PutMapping("{id}")
    @Operation(
            summary = "Atualiza um serviço",
            description = "Atualiza nome, valor e tempo médio estimado de um serviço já cadastrado. "
                    + "Serviços já incluídos em ordens de serviço mantêm o valor da época da inclusão."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    ResponseEntity<?> atualizar(@PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id,
                                @Valid ServicoRequestIn request);

    @PatchMapping("{id}/inativar")
    @Operation(
            summary = "Inativa um serviço",
            description = "Marca o serviço como inativo sem excluir o registro. Serviços inativos não podem ser "
                    + "adicionados a novas ordens de serviço."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Serviço inativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido"),
            @ApiResponse(responseCode = "404", description = "Serviço não encontrado")
    })
    ResponseEntity<?> inativar(@PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id);
}
