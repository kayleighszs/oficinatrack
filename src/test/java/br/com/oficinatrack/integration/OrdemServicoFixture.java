package br.com.oficinatrack.integration;

import br.com.oficinatrack.IntegrationTestBase;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class OrdemServicoFixture extends IntegrationTestBase {

    private static final AtomicInteger SEQ = new AtomicInteger(1);

    protected record Cenario(long osId, String numeroOs, long orcamentoId, long pecaId, String tokenPublico) {}

    /** Monta uma OS com orçamento ENVIADO que consome 'quantidade' unidades de uma peça nova com 'estoque'. */
    protected Cenario montarOrcamentoEnviado(String gestor, int estoque, int quantidade) throws Exception {
        int n = SEQ.getAndIncrement();
        String cpf = cpfValido(n);
        String placa = String.format("ABC%04d", n);

        postJson("/clientes", gestor, """
                {"nome":"Cliente %d","cpfCnpj":"%s","telefone":"11999990000","email":"c%d@teste.com"}
                """.formatted(n, cpf, n));
        long clienteId = lerNumero(get("/clientes").param("cpfCnpj", cpf), gestor, "$[0].id");

        long veiculoId = idDe(postJson("/veiculos", gestor, """
                {"clienteId":%d,"placa":"%s","marca":"Fiat","modelo":"Uno","ano":2015}
                """.formatted(clienteId, placa)));

        long servicoId = idDe(postJson("/servicos", gestor, """
                {"nome":"Troca de óleo %d","valor":120.00,"tempoMedioEstimado":30}
                """.formatted(n)));

        long pecaId = idDe(postJson("/pecas", gestor, """
                {"nome":"Peça %d","valorUnitario":35.90,"quantidadeEstoque":%d,"estoqueMinimo":1}
                """.formatted(n, estoque)));

        String os = postJson("/ordens-servico", gestor, """
                {"clienteId":%d,"veiculoId":%d}
                """.formatted(clienteId, veiculoId));
        long osId = idDe(os);
        String numeroOs = JsonPath.read(os, "$.numeroOs");

        postJson("/ordens-servico/" + osId + "/diagnosticos", gestor, null);
        postJson("/ordens-servico/" + osId + "/itens-servico", gestor, """
                {"referenciaId":%d,"quantidade":1}
                """.formatted(servicoId));
        postJson("/ordens-servico/" + osId + "/itens-peca", gestor, """
                {"referenciaId":%d,"quantidade":%d}
                """.formatted(pecaId, quantidade));

        String calculada = postJson("/ordens-servico/" + osId + "/orcamentos", gestor, null);
        long orcamentoId = ((Number) JsonPath.read(calculada, "$.orcamentos[0].id")).longValue();

        postJson("/ordens-servico/" + osId + "/orcamentos/" + orcamentoId + "/envios", gestor, null);

        String acesso = postJson("/ordens-servico/" + osId + "/acessos-publicos", gestor, null);
        String tokenPublico = JsonPath.read(acesso, "$.token");

        return new Cenario(osId, numeroOs, orcamentoId, pecaId, tokenPublico);
    }

    /** Valor do header Authorization nas chamadas públicas. Se vier 401, tente só o token, sem "Bearer ". */
    protected String cabecalhoPublico(String token) {
        return "Bearer " + token;
    }

    protected String postJson(String url, String token, String body) throws Exception {
        var req = post(url).header("Authorization", "Bearer " + token);
        if (body != null) {
            req = req.contentType(MediaType.APPLICATION_JSON).content(body);
        }
        return mockMvc.perform(req)
                .andExpect(status().is2xxSuccessful())
                .andReturn().getResponse().getContentAsString();
    }

    private long lerNumero(MockHttpServletRequestBuilder req, String token, String path) throws Exception {
        String json = mockMvc.perform(req.header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(json, path)).longValue();
    }

    private long idDe(String json) {
        return ((Number) JsonPath.read(json, "$.id")).longValue();
    }

    /** CPF válido (dígitos verificadores corretos) a partir de uma semente. */
    private static String cpfValido(int seed) {
        String base = String.format("%09d", 100000000 + seed);
        int d1 = digito(base, 10);
        int d2 = digito(base + d1, 11);
        return base + d1 + d2;
    }

    private static int digito(String digitos, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < digitos.length(); i++) {
            soma += (digitos.charAt(i) - '0') * (pesoInicial - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}