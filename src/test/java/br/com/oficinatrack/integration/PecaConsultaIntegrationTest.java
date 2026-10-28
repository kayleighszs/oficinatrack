package br.com.oficinatrack.integration;

import br.com.oficinatrack.IntegrationTestBase;
import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import br.com.oficinatrack.estoque.domain.Origem;
import br.com.oficinatrack.estoque.domain.TipoMovimentacao;
import br.com.oficinatrack.estoque.infrastructure.MovimentacaoEstoqueRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PecaConsultaIntegrationTest extends IntegrationTestBase {

    @Autowired
    MovimentacaoEstoqueRepository movimentacaoRepository;

    private long criarPeca(String token, String nome, int estoque, int minimo) throws Exception {
        String resposta = mockMvc.perform(post("/pecas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"%s","valorUnitario":25.00,
                                 "quantidadeEstoque":%d,"estoqueMinimo":%d}
                                """.formatted(nome, estoque, minimo)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    private String nomeUnico(String prefixo) {
        return prefixo + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    // ---------- item 2: ENTRADA e movimentação gravada ----------

    @Test
    @DisplayName("ENTRADA soma ao estoque e grava a movimentação MANUAL")
    void entradaSomaEGravaMovimentacao() throws Exception {
        String token = tokenGestor();
        long id = criarPeca(token, nomeUnico("Correia"), 10, 3);

        mockMvc.perform(patch("/pecas/{id}/estoque", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"ENTRADA","quantidade":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(15));

        List<MovimentacaoEstoque> movimentacoes = movimentacaoRepository.findAll().stream()
                .filter(m -> m.getPeca().getId().equals(id))
                .toList();

        assertThat(movimentacoes).hasSize(1);
        assertThat(movimentacoes.get(0).getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
        assertThat(movimentacoes.get(0).getOrigem()).isEqualTo(Origem.MANUAL);
        assertThat(movimentacoes.get(0).getQuantidade()).isEqualTo(5);
    }


    // ---------- item 3: GET por id e listagem ----------

    @Test
    @DisplayName("GET por id devolve a peça cadastrada")
    void buscarPorIdDevolvePeca() throws Exception {
        String token = tokenGestor();
        String nome = nomeUnico("Bateria");
        long id = criarPeca(token, nome, 4, 1);

        mockMvc.perform(get("/pecas/{id}", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome").value(nome))
                .andExpect(jsonPath("$.quantidadeEstoque").value(4));
    }

    @Test
    @DisplayName("GET por id inexistente devolve 404")
    void buscarPorIdInexistenteDevolve404() throws Exception {
        String token = tokenGestor();

        mockMvc.perform(get("/pecas/{id}", 999999L).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("listagem filtra por nome")
    void listarFiltraPorNome() throws Exception {
        String token = tokenGestor();
        String nome = nomeUnico("Amortecedor");
        criarPeca(token, nome, 8, 2);
        criarPeca(token, nomeUnico("Outra"), 8, 2);

        mockMvc.perform(get("/pecas")
                        .param("nome", nome)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value(nome));
    }

    @Test
    @DisplayName("listagem com estoqueBaixo=true traz a peça abaixo do mínimo")
    void listarEstoqueBaixo() throws Exception {
        String token = tokenGestor();
        String nome = nomeUnico("Junta");
        criarPeca(token, nome, 1, 5);

        mockMvc.perform(get("/pecas")
                        .param("nome", nome)
                        .param("estoqueBaixo", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$.content[0].nome").value(nome));
    }

    @Test
    @DisplayName("listagem sem filtros devolve 200")
    void listarSemFiltros() throws Exception {
        String token = tokenGestor();
        criarPeca(token, nomeUnico("Qualquer"), 5, 1);

        mockMvc.perform(get("/pecas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isNotEmpty());
    }

    // ---------- item 4: validação do body ----------

    @Test
    @DisplayName("cadastrar com dados inválidos devolve 400")
    void cadastrarInvalidoDevolve400() throws Exception {
        String token = tokenGestor();

        mockMvc.perform(post("/pecas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","valorUnitario":-5,
                                 "quantidadeEstoque":-1,"estoqueMinimo":-1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("movimentar com tipo ausente devolve 400")
    void movimentarSemTipoDevolve400() throws Exception {
        String token = tokenGestor();
        long id = criarPeca(token, nomeUnico("Filtro"), 5, 1);

        mockMvc.perform(patch("/pecas/{id}/estoque", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantidade":3}
                                """))
                .andExpect(status().isBadRequest());
    }
}