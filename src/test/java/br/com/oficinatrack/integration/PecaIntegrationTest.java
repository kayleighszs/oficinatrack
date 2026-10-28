package br.com.oficinatrack.integration;

import br.com.oficinatrack.IntegrationTestBase;
import br.com.oficinatrack.auth.infra.UsuarioRepository;
import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import br.com.oficinatrack.estoque.domain.Origem;
import br.com.oficinatrack.estoque.domain.TipoMovimentacao;
import br.com.oficinatrack.estoque.infrastructure.MovimentacaoEstoqueRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import br.com.oficinatrack.auth.domain.Usuario;
import br.com.oficinatrack.auth.domain.Role;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PecaIntegrationTest extends IntegrationTestBase {

    @Autowired
    MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("cadastra peça, movimenta estoque e registra a movimentação com o usuário autenticado")
    void fluxoCompletoDeEstoque() throws Exception {
        Usuario outroGestor = usuarioRepository.save(
                new Usuario("gestor-" + System.nanoTime(), passwordEncoder.encode("senha123"), Role.GESTOR));

        String token = token(outroGestor.getUsername(), "senha123");

        String criada = mockMvc.perform(post("/pecas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Filtro de óleo","valorUnitario":35.90,
                                 "quantidadeEstoque":10,"estoqueMinimo":3}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = ((Number) JsonPath.read(criada, "$.id")).longValue();

        mockMvc.perform(patch("/pecas/{id}/estoque", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipo":"SAIDA","quantidade":4}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(6));

        Long usuarioId = outroGestor.getId();

        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll().stream()
                .filter(m -> m.getPeca().getId().equals(id))
                .toList();

        assertThat(movimentacoes).hasSize(1);
        assertThat(movimentacoes.get(0).getOrigem()).isEqualTo(Origem.MANUAL);
        assertThat(movimentacoes.get(0).getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
        assertThat(movimentacoes.get(0).getQuantidade()).isEqualTo(4);
        assertThat(movimentacoes.get(0).getUsuarioId()).isEqualTo(usuarioId);
    }

    @Test
    @DisplayName("retorna erro de regra de negócio quando a saída deixaria o estoque negativo")
    void saidaMaiorQueEstoque() throws Exception {
        String token = tokenGestor();

        String criada = mockMvc.perform(post("/pecas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"nome":"Vela de ignição","valorUnitario":20.00,
                             "quantidadeEstoque":2,"estoqueMinimo":1}
                            """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = ((Number) JsonPath.read(criada, "$.id")).longValue();

        mockMvc.perform(patch("/pecas/{id}/estoque", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"tipo":"SAIDA","quantidade":10}
                            """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    @DisplayName("nega acesso sem token")
    void semTokenRetorna401ou403() throws Exception {
        mockMvc.perform(get("/pecas"))
                .andExpect(status().is4xxClientError());
    }
}
