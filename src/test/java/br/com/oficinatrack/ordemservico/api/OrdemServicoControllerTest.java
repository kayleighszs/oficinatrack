package br.com.oficinatrack.ordemservico.api;

import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoResponse;
import br.com.oficinatrack.ordemservico.application.OrdemServicoService;
import br.com.oficinatrack.ordemservico.domain.StatusOrdemServico;
import br.com.oficinatrack.shared.exception.GlobalExceptionHandler;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrdemServicoControllerTest {

    @Mock
    private OrdemServicoService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new OrdemServicoController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private OrdemServicoResponse response(StatusOrdemServico status, BigDecimal total) {
        return new OrdemServicoResponse(1L, 10L, 20L, status, total, List.of(), List.of(), null, null);
    }

    // ---------- POST /ordens-servico ----------

    @Test
    void criarDeveRetornar201ComLocationEStatusRecebida() throws Exception {
        when(service.criar(any())).thenReturn(response(StatusOrdemServico.RECEBIDA, new BigDecimal("0.00")));

        mockMvc.perform(post("/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"veiculoId\":20}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/ordens-servico/1"))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.valorTotal").value(0.00));
    }

    @Test
    void criarSemCamposObrigatoriosDeveRetornar400() throws Exception {
        mockMvc.perform(post("/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(2));

        verify(service, never()).criar(any());
    }

    @Test
    void criarComClienteInativoDeveRetornar400() throws Exception {
        when(service.criar(any())).thenThrow(new RegraDeNegocioException("cliente inexistente ou inativo: 10"));

        mockMvc.perform(post("/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\":10,\"veiculoId\":20}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("cliente inexistente ou inativo: 10"));
    }

    // ---------- POST /ordens-servico/{id}/servicos ----------

    @Test
    void adicionarServicoDeveRetornar200() throws Exception {
        when(service.adicionarServico(eq(1L), any()))
                .thenReturn(response(StatusOrdemServico.RECEBIDA, new BigDecimal("160.00")));

        mockMvc.perform(post("/ordens-servico/1/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoId\":5,\"quantidade\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(160.00));
    }

    @Test
    void adicionarServicoComQuantidadeInvalidaDeveRetornar400() throws Exception {
        mockMvc.perform(post("/ordens-servico/1/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoId\":5,\"quantidade\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("quantidade"));

        verify(service, never()).adicionarServico(any(), any());
    }

    @Test
    void adicionarServicoEmOsInexistenteDeveRetornar404() throws Exception {
        when(service.adicionarServico(eq(99L), any()))
                .thenThrow(new RecursoNaoEncontradoException("ordem de serviço não encontrada com o id: 99"));

        mockMvc.perform(post("/ordens-servico/99/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoId\":5,\"quantidade\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void adicionarServicoComStatusInvalidoDeveRetornar400() throws Exception {
        when(service.adicionarServico(eq(1L), any()))
                .thenThrow(new RegraDeNegocioException("não é possível incluir itens em uma OS com status EM_EXECUCAO"));

        mockMvc.perform(post("/ordens-servico/1/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"servicoId\":5,\"quantidade\":1}"))
                .andExpect(status().isBadRequest());
    }

    // ---------- POST /ordens-servico/{id}/pecas ----------

    @Test
    void adicionarPecaDeveRetornar200() throws Exception {
        when(service.adicionarPeca(eq(1L), any()))
                .thenReturn(response(StatusOrdemServico.RECEBIDA, new BigDecimal("105.75")));

        mockMvc.perform(post("/ordens-servico/1/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pecaId\":7,\"quantidade\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(105.75));
    }

    @Test
    void adicionarPecaComEstoqueInsuficienteDeveRetornar400() throws Exception {
        when(service.adicionarPeca(eq(1L), any()))
                .thenThrow(new RegraDeNegocioException("estoque insuficiente para a peça 'Filtro'"));

        mockMvc.perform(post("/ordens-servico/1/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pecaId\":7,\"quantidade\":300}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("estoque insuficiente para a peça 'Filtro'"));
    }

    @Test
    void adicionarPecaSemPecaIdDeveRetornar400() throws Exception {
        mockMvc.perform(post("/ordens-servico/1/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("pecaId"));
    }

    @Test
    void adicionarPecaEmOsInexistenteDeveRetornar404() throws Exception {
        when(service.adicionarPeca(eq(99L), any()))
                .thenThrow(new RecursoNaoEncontradoException("ordem de serviço não encontrada com o id: 99"));

        mockMvc.perform(post("/ordens-servico/99/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pecaId\":7,\"quantidade\":1}"))
                .andExpect(status().isNotFound());
    }

    // ---------- POST /ordens-servico/{id}/orcamento/enviar ----------

    @Test
    void enviarOrcamentoDeveRetornar200ComStatusAguardandoAprovacao() throws Exception {
        when(service.enviarOrcamento(1L))
                .thenReturn(response(StatusOrdemServico.AGUARDANDO_APROVACAO, new BigDecimal("80.00")));

        mockMvc.perform(post("/ordens-servico/1/orcamento/enviar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));
    }

    @Test
    void enviarOrcamentoSemItensDeveRetornar400() throws Exception {
        when(service.enviarOrcamento(1L))
                .thenThrow(new RegraDeNegocioException("a OS precisa ter ao menos um item para enviar o orçamento"));

        mockMvc.perform(post("/ordens-servico/1/orcamento/enviar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void enviarOrcamentoEmOsInexistenteDeveRetornar404() throws Exception {
        when(service.enviarOrcamento(99L))
                .thenThrow(new RecursoNaoEncontradoException("ordem de serviço não encontrada com o id: 99"));

        mockMvc.perform(post("/ordens-servico/99/orcamento/enviar"))
                .andExpect(status().isNotFound());
    }
}
