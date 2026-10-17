package br.com.oficinatrack.publico;

import br.com.oficinatrack.TestcontainersConfiguration;
import br.com.oficinatrack.cliente.domain.*;
import br.com.oficinatrack.cliente.infra.ClienteRepository;
import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.infrastructure.*;
import br.com.oficinatrack.ordemservico.domain.*;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.publico.api.dto.*;
import br.com.oficinatrack.publico.application.*;
import br.com.oficinatrack.veiculo.domain.*;
import br.com.oficinatrack.veiculo.infra.VeiculoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "api.security.token.secret=segredo-de-testes-com-trinta-e-dois-caracteres")
@Transactional
class ConsultaPublicaIntegracaoTest {
    @Autowired ClienteRepository clienteRepository;
    @Autowired VeiculoRepository veiculoRepository;
    @Autowired PecaRepository pecaRepository;
    @Autowired OrdemServicoRepository ordemRepository;
    @Autowired MovimentacaoEstoqueRepository movimentacaoRepository;
    @Autowired AcessoPublicoService acessoService;
    @Autowired ConsultaPublicaService consultaService;

    @Test
    void deveAprovarOrcamentoEBaixarEstoqueUmaUnicaVez() {
        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setCpfCnpj("52998224725");
        cliente.setTipoPessoa(TipoPessoa.FISICA);
        cliente = clienteRepository.saveAndFlush(cliente);

        Veiculo veiculo = new Veiculo();
        veiculo.setMarca("Fiat");
        veiculo.setModelo("Uno");
        veiculo.setAno(2020);
        veiculo.setPlaca(new Placa("ABC1D23"));
        veiculo.setCliente(cliente);
        veiculo = veiculoRepository.saveAndFlush(veiculo);

        Peca peca = pecaRepository.saveAndFlush(Peca.cadastrar(
                "Filtro", new BigDecimal("20.00"), 10, 2));

        OrdemServico os = OrdemServico.abrir(cliente.getId(), veiculo.getId());
        os.iniciarDiagnostico();
        os.adicionarPeca(peca.getId(), peca.getNome(), peca.getValorUnitario(), 3);
        Orcamento orcamento = os.calcularOrcamento();
        ordemRepository.saveAndFlush(os);
        os.enviarOrcamento(orcamento.getId());
        ordemRepository.saveAndFlush(os);

        AcessoPublicoResponse acesso = acessoService.emitir(os.getId());
        DecisaoOrcamentoRequest decisao = new DecisaoOrcamentoRequest(
                DecisaoOrcamentoRequest.Decisao.APROVADO, null);

        consultaService.decidir("Bearer " + acesso.token(), os.getNumeroOs(), orcamento.getId(), decisao);
        consultaService.decidir("Bearer " + acesso.token(), os.getNumeroOs(), orcamento.getId(), decisao);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, os.getStatus());
        assertEquals(7, pecaRepository.findById(peca.getId()).orElseThrow().getQuantidadeEstoque());
        assertEquals(1, movimentacaoRepository.count());
    }
}
