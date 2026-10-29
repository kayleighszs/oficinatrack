package br.com.oficinatrack.integration;

import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import br.com.oficinatrack.estoque.domain.Origem;
import br.com.oficinatrack.estoque.infrastructure.MovimentacaoEstoqueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BaixaEstoqueIntegrationTest extends OrdemServicoFixture {

    @Autowired
    MovimentacaoEstoqueRepository movimentacaoRepository;

    private String corpo(String decisao) {
        return """
                {"decisao":"%s","motivo":"teste"}
                """.formatted(decisao);
    }

    private org.springframework.test.web.servlet.ResultActions decidir(Cenario c, String decisao) throws Exception {
        return mockMvc.perform(post("/publico/ordens-servico/{n}/orcamentos/{o}/decisoes",
                c.numeroOs(), c.orcamentoId())
                .header("Authorization", cabecalhoPublico(c.tokenPublico()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(decisao)));
    }

    private List<MovimentacaoEstoque> baixasDa(long pecaId) {
        return movimentacaoRepository.findAll().stream()
                .filter(m -> m.getPeca().getId().equals(pecaId))
                .filter(m -> m.getOrigem() == Origem.BAIXA_OS)
                .toList();
    }

    private void assertEstoque(String gestor, long pecaId, int esperado) throws Exception {
        mockMvc.perform(get("/pecas/{id}", pecaId).header("Authorization", "Bearer " + gestor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(esperado));
    }

    @Test
    @DisplayName("aprovar o orçamento baixa o estoque e registra a movimentação BAIXA_OS")
    void aprovarBaixaEstoque() throws Exception {
        String gestor = tokenGestor();
        Cenario c = montarOrcamentoEnviado(gestor, 10, 4);

        decidir(c, "APROVADO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamentos[0].status").value("APROVADO"))
                .andExpect(jsonPath("$.historico[3].novo").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.historico[3].id").isNumber());

        assertEstoque(gestor, c.pecaId(), 6);

        List<MovimentacaoEstoque> baixas = baixasDa(c.pecaId());
        assertThat(baixas).hasSize(1);
        assertThat(baixas.get(0).getQuantidade()).isEqualTo(4);
        assertThat(baixas.get(0).getOrdemServicoId()).isEqualTo(c.osId());
        assertThat(baixas.get(0).getOrcamentoId()).isEqualTo(c.orcamentoId());
    }

    @Test
    @DisplayName("estoque insuficiente na aprovação devolve 409 e desfaz tudo (atomicidade)")
    void aprovarSemEstoqueFazRollback() throws Exception {
        String gestor = tokenGestor();
        Cenario c = montarOrcamentoEnviado(gestor, 5, 5);

        // alguém retira 4 unidades antes da aprovação: sobra 1, o orçamento pede 5
        mockMvc.perform(patch("/pecas/{id}/estoque", c.pecaId())
                        .header("Authorization", "Bearer " + gestor)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"SAIDA","quantidade":4}
                                """))
                .andExpect(status().isOk());

        decidir(c, "APROVADO").andExpect(status().isConflict());

        assertEstoque(gestor, c.pecaId(), 1);
        assertThat(baixasDa(c.pecaId())).isEmpty();

        mockMvc.perform(get("/ordens-servico/{id}", c.osId()).header("Authorization", "Bearer " + gestor))
                .andExpect(jsonPath("$.orcamentos[0].status").value("ENVIADO"));
    }

    @Test
    @DisplayName("aprovar duas vezes o mesmo orçamento não baixa o estoque de novo")
    void aprovarDuasVezesNaoDuplicaBaixa() throws Exception {
        String gestor = tokenGestor();
        Cenario c = montarOrcamentoEnviado(gestor, 10, 4);

        decidir(c, "APROVADO").andExpect(status().isOk());
        decidir(c, "APROVADO").andExpect(status().isOk());

        assertEstoque(gestor, c.pecaId(), 6);
        assertThat(baixasDa(c.pecaId())).hasSize(1);
    }

    @Test
    @DisplayName("recusar o orçamento não mexe no estoque")
    void recusarNaoMexeNoEstoque() throws Exception {
        String gestor = tokenGestor();
        Cenario c = montarOrcamentoEnviado(gestor, 10, 4);

        decidir(c, "RECUSADO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamentos[0].status").value("RECUSADO"))
                .andExpect(jsonPath("$.historico[3].id").isNumber());

        assertEstoque(gestor, c.pecaId(), 10);
        assertThat(baixasDa(c.pecaId())).isEmpty();
    }
}