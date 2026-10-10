package br.com.oficinatrack.servico;

import br.com.oficinatrack.catalogoservicos.api.ServicoController;
import br.com.oficinatrack.catalogoservicos.api.dto.out.ServicoRequestOut;
import br.com.oficinatrack.catalogoservicos.application.ServicoService;
import br.com.oficinatrack.shared.exception.GlobalExceptionHandler;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ServicoControllerTest {

    @Mock
    private ServicoService servicoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ServicoController(servicoService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(validator)
                .build();
    }

    private ServicoRequestOut responseOut() {
        return new ServicoRequestOut(
                1L,
                "Troca de Óleo",
                new BigDecimal("150.00"),
                45,
                true
        );
    }

    // ---------- POST /servicos ----------

    @Test
    void cadastrarDeveRetornar200() throws Exception {
        when(servicoService.cadastrar(any()))
                .thenReturn((ResponseEntity) ResponseEntity.ok(responseOut()));

        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nome": "Troca de Óleo",
                                "valor": 150.00,
                                "tempoMedioEstimado": 45
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Troca de Óleo"))
                .andExpect(jsonPath("$.valor").value(150.00))
                .andExpect(jsonPath("$.tempoMedioEstimado").value(45));
    }

    @Test
    void cadastrarSemNomeEValorInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "",
                                    "valor": -10.00,
                                    "tempoMedioEstimado": 30
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(servicoService, never()).cadastrar(any());
    }

    @Test
    void cadastrarComJsonMalformadoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nome:"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cadastrarServicoComRegraDeNegocioInvalidaDeveRetornar400() throws Exception {
        doThrow(new RegraDeNegocioException("já existe um serviço cadastrado com este nome"))
                .when(servicoService).cadastrar(any());

        mockMvc.perform(post("/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Troca de Óleo",
                                    "valor": 150.00,
                                    "tempoMedioEstimado": 45
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ---------- GET /servicos ----------

    @Test
    void listarDeveRetornar200ComPaginaDeServicos() throws Exception {
        var pageable = PageRequest.of(0, 10);
        var page = new PageImpl<>(List.of(responseOut()), pageable, 1);

        Map<String, Object> mapResponse = new HashMap<>();
        mapResponse.put("ordens", page);
        mapResponse.put("currentPage", 0);
        mapResponse.put("totalItens", 1L);
        mapResponse.put("totalPages", 1);

        when(servicoService.listar(eq("Troca"), any())).thenReturn(ResponseEntity.ok(mapResponse));

        mockMvc.perform(get("/servicos")
                        .param("nome", "Troca")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordens.content[0].id").value(1))
                .andExpect(jsonPath("$.ordens.content[0].nome").value("Troca de Óleo"))
                .andExpect(jsonPath("$.ordens.content[0].valor").value(150.00))
                .andExpect(jsonPath("$.totalItens").value(1));
    }

    @Test
    void listarSemFiltrosDeveRetornarPaginaVazia() throws Exception {
        var pageable = PageRequest.of(0, 10);
        var pageVazia = new PageImpl<ServicoRequestOut>(List.of(), pageable, 0);

        Map<String, Object> mapResponse = new HashMap<>();
        mapResponse.put("ordens", pageVazia);
        mapResponse.put("currentPage", 0);
        mapResponse.put("totalItens", 0L);
        mapResponse.put("totalPages", 0);

        when(servicoService.listar(eq(null), any())).thenReturn(ResponseEntity.ok(mapResponse));

        mockMvc.perform(get("/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordens.content.length()").value(0))
                .andExpect(jsonPath("$.totalItens").value(0));
    }

    // ---------- GET /servicos/{id} ----------

    @Test
    void buscarPorIdDeveRetornar200() throws Exception {
        when(servicoService.buscarPorId(1L)).thenReturn(responseOut());

        mockMvc.perform(get("/servicos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Troca de Óleo"))
                .andExpect(jsonPath("$.valor").value(150.00))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() throws Exception {
        when(servicoService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Serviço não encontrado com o ID: 99"));

        mockMvc.perform(get("/servicos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorIdNaoNumericoDeveRetornar400() throws Exception {
        mockMvc.perform(get("/servicos/abc"))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT /servicos/{id} ----------

    @Test
    void atualizarDeveRetornar200() throws Exception {
        when(servicoService.atualizar(eq(1L), any())).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(put("/servicos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Alinhamento e Balanceamento",
                                    "valor": 200.00,
                                    "tempoMedioEstimado": 60
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void atualizarServicoInexistenteDeveRetornar404() throws Exception {
        when(servicoService.atualizar(eq(99L), any()))
                .thenThrow(new RecursoNaoEncontradoException("Serviço não encontrado com o ID: 99"));

        mockMvc.perform(put("/servicos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Troca de Filtro",
                                    "valor": 80.00,
                                    "tempoMedioEstimado": 20
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /servicos/{id}/inativar ----------

    @Test
    void inativarDeveRetornar200() throws Exception {
        when(servicoService.inativar(1L)).thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/servicos/1/inativar"))
                .andExpect(status().isOk());

        verify(servicoService).inativar(1L);
    }

    @Test
    void inativarServicoInexistenteDeveRetornar404() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Serviço não encontrado com o ID: 99"))
                .when(servicoService).inativar(99L);

        mockMvc.perform(patch("/servicos/99/inativar"))
                .andExpect(status().isNotFound());
    }
}