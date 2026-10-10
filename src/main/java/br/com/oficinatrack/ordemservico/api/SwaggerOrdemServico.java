package br.com.oficinatrack.ordemservico.api;

import br.com.oficinatrack.ordemservico.api.dto.ItemPecaRequest;
import br.com.oficinatrack.ordemservico.api.dto.ItemServicoRequest;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoRequest;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Ordens de Serviço")
public interface SwaggerOrdemServico {

    @PostMapping
    @Operation(
            summary = "Cria uma ordem de serviço",
            description = "Abre uma OS com status inicial RECEBIDA. Cliente e veículo devem existir e estar ativos, "
                    + "e o veículo deve pertencer ao cliente. Publica o evento interno OrdemServicoCriada."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Ordem de serviço criada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrdemServicoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos, ou cliente/veículo inexistente ou inativo"
            )
    })
    ResponseEntity<OrdemServicoResponse> criar(@Valid OrdemServicoRequest request);

    @PostMapping("{id}/servicos")
    @Operation(
            summary = "Adiciona um serviço à OS",
            description = "Permitido apenas nos status RECEBIDA ou EM_DIAGNOSTICO. O valor e a descrição do serviço "
                    + "são copiados do catálogo no momento da inclusão e o valor total é recalculado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Serviço adicionado; retorna a OS atualizada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrdemServicoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Status inválido para a ação, serviço inexistente ou inativo, ou dados inválidos"
            ),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    ResponseEntity<OrdemServicoResponse> adicionarServico(
            @PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id,
            @Valid ItemServicoRequest request);

    @PostMapping("{id}/pecas")
    @Operation(
            summary = "Adiciona uma peça à OS",
            description = "Valida a disponibilidade em estoque (considerando o que já foi incluído na própria OS). "
                    + "Permitido apenas nos status RECEBIDA ou EM_DIAGNOSTICO. O valor e a descrição da peça são "
                    + "copiados no momento da inclusão e o valor total é recalculado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Peça adicionada; retorna a OS atualizada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrdemServicoResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Estoque insuficiente, status inválido, peça inexistente ou inativa, ou dados inválidos"
            ),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    ResponseEntity<OrdemServicoResponse> adicionarPeca(
            @PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id,
            @Valid ItemPecaRequest request);

    @PostMapping("{id}/orcamento/enviar")
    @Operation(
            summary = "Envia o orçamento para aprovação",
            description = "Exige ao menos um item incluído. Muda o status para AGUARDANDO_APROVACAO e bloqueia "
                    + "novas inclusões de serviços e peças."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Orçamento enviado; retorna a OS com status AGUARDANDO_APROVACAO",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OrdemServicoResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "OS sem itens ou status inválido"),
            @ApiResponse(responseCode = "404", description = "Ordem de serviço não encontrada")
    })
    ResponseEntity<OrdemServicoResponse> enviarOrcamento(
            @PathVariable("id") @Positive(message = "ID deve ser maior que zero") Long id);
}
