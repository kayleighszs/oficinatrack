package br.com.oficinatrack.cliente.api;

import br.com.oficinatrack.cliente.api.dto.ClienteResponse;
import br.com.oficinatrack.cliente.application.ClienteService;
import br.com.oficinatrack.cliente.domain.TipoPessoa;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new ClienteController(clienteService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private ClienteResponse response() {
        ClienteResponse response = new ClienteResponse();
        response.setId(1L);
        response.setNome("João da Silva");
        response.setCpfCnpj("52998224725");
        response.setTipoPessoa(TipoPessoa.FISICA);
        response.setAtivo(true);
        return response;
    }

    // ---------- POST /clientes ----------

    @Test
    void cadastrarDeveRetornar201() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"João","cpfCnpj":"529.982.247-25","telefone":"(11) 91234-5678","email":"joao@email.com"}
                                """))
                .andExpect(status().isCreated());

        verify(clienteService).cadastrar(any());
    }

    @Test
    void cadastrarSemNomeDeveRetornar400ComCampoDetalhado() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"cpfCnpj":"529.982.247-25"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos[0].campo").value("nome"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("Nome do cliente é obrigatório"));

        verify(clienteService, never()).cadastrar(any());
    }

    @Test
    void cadastrarComCpfInvalidoEEmailInvalidoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"João","cpfCnpj":"111.111.111-11","email":"nao-e-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(2));

        verify(clienteService, never()).cadastrar(any());
    }

    @Test
    void cadastrarComJsonMalformadoDeveRetornar400() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nome:"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void cadastrarDocumentoDuplicadoDeveRetornar400() throws Exception {
        org.mockito.Mockito.doThrow(new RegraDeNegocioException("já existe um cliente cadastrado com o CPF/CNPJ informado"))
                .when(clienteService).cadastrar(any());

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"João","cpfCnpj":"52998224725"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("já existe um cliente cadastrado com o CPF/CNPJ informado"));
    }

    // ---------- GET /clientes ----------

    @Test
    void listarDeveRetornar200ComClientes() throws Exception {
        when(clienteService.listarClientes("529", "joão")).thenReturn(List.of(response()));

        mockMvc.perform(get("/clientes").param("cpfCnpj", "529").param("nome", "joão"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("João da Silva"))
                .andExpect(jsonPath("$[0].tipoPessoa").value("FISICA"))
                .andExpect(jsonPath("$[0].ativo").value(true));
    }

    @Test
    void listarSemFiltrosDeveRetornarListaVazia() throws Exception {
        when(clienteService.listarClientes(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ---------- GET /clientes/{id} ----------

    @Test
    void buscarPorIdDeveRetornar200() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(response());

        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpfCnpj").value("52998224725"));
    }

    @Test
    void buscarPorIdInexistenteDeveRetornar404() throws Exception {
        when(clienteService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("cliente não encontrado com o id: 99"));

        mockMvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("cliente não encontrado com o id: 99"));
    }

    @Test
    void buscarPorIdNaoNumericoDeveRetornar400() throws Exception {
        mockMvc.perform(get("/clientes/abc"))
                .andExpect(status().isBadRequest());
    }

    // ---------- PUT /clientes/{id} ----------

    @Test
    void atualizarDeveRetornar200() throws Exception {
        ClienteResponse atualizado = response();
        atualizado.setNome("João Atualizado");
        when(clienteService.atualizar(org.mockito.ArgumentMatchers.eq(1L), any())).thenReturn(atualizado);

        mockMvc.perform(put("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"João Atualizado","telefone":"11 98888-7777","email":"novo@email.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("João Atualizado"));
    }

    @Test
    void atualizarComDadosInvalidosDeveRetornar400() throws Exception {
        mockMvc.perform(put("/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","telefone":"abc","email":"nao-e-email"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(3));

        verify(clienteService, never()).atualizar(any(), any());
    }

    @Test
    void atualizarClienteInexistenteDeveRetornar404() throws Exception {
        when(clienteService.atualizar(org.mockito.ArgumentMatchers.eq(99L), any()))
                .thenThrow(new RecursoNaoEncontradoException("cliente não encontrado com o id: 99"));

        mockMvc.perform(put("/clientes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Fulano"}
                                """))
                .andExpect(status().isNotFound());
    }

    // ---------- PATCH /clientes/{id}/inativar ----------

    @Test
    void inativarDeveRetornar200ComAtivoFalso() throws Exception {
        ClienteResponse inativo = response();
        inativo.setAtivo(false);
        when(clienteService.inativar(1L)).thenReturn(inativo);

        mockMvc.perform(patch("/clientes/1/inativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativo").value(false));
    }

    @Test
    void inativarClienteInexistenteDeveRetornar404() throws Exception {
        when(clienteService.inativar(99L))
                .thenThrow(new RecursoNaoEncontradoException("cliente não encontrado com o id: 99"));

        mockMvc.perform(patch("/clientes/99/inativar"))
                .andExpect(status().isNotFound());
    }
}
