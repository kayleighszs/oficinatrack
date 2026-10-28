package br.com.oficinatrack.integration;

import br.com.oficinatrack.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PecaPerfisIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("ATENDENTE pode listar peças")
    void atendentePodeListar() throws Exception {
        String token = token("atendente", "atendente123");

        mockMvc.perform(get("/pecas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("MECANICO pode listar peças")
    void mecanicoPodeListar() throws Exception {
        String token = token("mecanico", "mecanico123");

        mockMvc.perform(get("/pecas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ATENDENTE não pode cadastrar peça")
    void atendenteNaoPodeCadastrar() throws Exception {
        String token = token("atendente", "atendente123");

        mockMvc.perform(post("/pecas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Filtro","valorUnitario":10.00,
                                 "quantidadeEstoque":5,"estoqueMinimo":1}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MECANICO não pode movimentar estoque")
    void mecanicoNaoPodeMovimentarEstoque() throws Exception {
        String token = token("mecanico", "mecanico123");

        mockMvc.perform(patch("/pecas/{id}/estoque", 1L)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"ENTRADA","quantidade":5}
                                """))
                .andExpect(status().isForbidden());
    }
}