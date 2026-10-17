package br.com.oficinatrack.ordemservico.application;

import br.com.oficinatrack.catalogoservicos.domain.Servico;
import br.com.oficinatrack.catalogoservicos.infra.ServicoRepository;
import br.com.oficinatrack.cliente.domain.Cliente;
import br.com.oficinatrack.cliente.infra.ClienteRepository;
import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.infrastructure.PecaRepository;
import br.com.oficinatrack.ordemservico.api.dto.*;
import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import br.com.oficinatrack.veiculo.domain.Veiculo;
import br.com.oficinatrack.veiculo.infra.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdemServicoService {
    private final OrdemServicoRepository ordemRepository;
    private final ClienteRepository clienteRepository;
    private final VeiculoRepository veiculoRepository;
    private final ServicoRepository servicoRepository;
    private final PecaRepository pecaRepository;

    public OrdemServicoService(OrdemServicoRepository ordemRepository, ClienteRepository clienteRepository,
                               VeiculoRepository veiculoRepository, ServicoRepository servicoRepository,
                               PecaRepository pecaRepository) {
        this.ordemRepository = ordemRepository;
        this.clienteRepository = clienteRepository;
        this.veiculoRepository = veiculoRepository;
        this.servicoRepository = servicoRepository;
        this.pecaRepository = pecaRepository;
    }

    @Transactional
    public OrdemServicoResponse criar(CriarOrdemServicoRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .filter(c -> Boolean.TRUE.equals(c.getAtivo()))
                .orElseThrow(() -> new RegraDeNegocioException("Cliente inexistente ou inativo."));
        Veiculo veiculo = veiculoRepository.findById(request.veiculoId())
                .filter(Veiculo::isAtivo)
                .orElseThrow(() -> new RegraDeNegocioException("Veículo inexistente ou inativo."));
        if (!veiculo.getCliente().getId().equals(cliente.getId()))
            throw new RegraDeNegocioException("Veículo não pertence ao cliente informado.");
        return OrdemServicoResponse.de(ordemRepository.save(OrdemServico.abrir(cliente.getId(), veiculo.getId())));
    }

    @Transactional
    public OrdemServicoResponse iniciarDiagnostico(Long id) {
        OrdemServico os = buscar(id);
        os.iniciarDiagnostico();
        return OrdemServicoResponse.de(os);
    }

    @Transactional
    public OrdemServicoResponse adicionarServico(Long id, AdicionarItemRequest request) {
        OrdemServico os = buscar(id);
        Servico servico = servicoRepository.findById(request.referenciaId())
                .filter(s -> Boolean.TRUE.equals(s.getAtivo()))
                .orElseThrow(() -> new RegraDeNegocioException("Serviço inexistente ou inativo."));
        os.adicionarServico(servico.getId(), servico.getNome(), servico.getValor(), request.quantidade());
        return OrdemServicoResponse.de(os);
    }

    @Transactional
    public OrdemServicoResponse adicionarPeca(Long id, AdicionarItemRequest request) {
        OrdemServico os = buscar(id);
        Peca peca = pecaRepository.findById(request.referenciaId())
                .filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                .orElseThrow(() -> new RegraDeNegocioException("Peça inexistente ou inativa."));
        os.adicionarPeca(peca.getId(), peca.getNome(), peca.getValorUnitario(), request.quantidade());
        return OrdemServicoResponse.de(os);
    }

    @Transactional
    public OrdemServicoResponse calcularOrcamento(Long id) {
        OrdemServico os = buscar(id);
        os.calcularOrcamento();
        ordemRepository.flush();
        return OrdemServicoResponse.de(os);
    }

    @Transactional
    public OrdemServicoResponse enviarOrcamento(Long id, Long orcamentoId) {
        OrdemServico os = buscar(id);
        os.enviarOrcamento(orcamentoId);
        return OrdemServicoResponse.de(os);
    }

    @Transactional
    public OrdemServicoResponse finalizar(Long id) {
        OrdemServico os = buscar(id);
        os.finalizarExecucao();
        return OrdemServicoResponse.de(os);
    }

    @Transactional
    public OrdemServicoResponse entregar(Long id) {
        OrdemServico os = buscar(id);
        os.confirmarEntrega();
        return OrdemServicoResponse.de(os);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse detalhar(Long id) { return OrdemServicoResponse.de(buscar(id)); }

    private OrdemServico buscar(Long id) {
        return ordemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada."));
    }
}
