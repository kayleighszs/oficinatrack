package br.com.oficinatrack.ordemservico.application;

import br.com.oficinatrack.ordemservico.api.dto.ItemPecaRequest;
import br.com.oficinatrack.ordemservico.api.dto.ItemServicoRequest;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoRequest;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoResponse;
import br.com.oficinatrack.ordemservico.application.port.ClienteConsultaPort;
import br.com.oficinatrack.ordemservico.application.port.EstoquePort;
import br.com.oficinatrack.ordemservico.application.port.EstoquePort.PecaEstoque;
import br.com.oficinatrack.ordemservico.application.port.ServicoCatalogoPort;
import br.com.oficinatrack.ordemservico.application.port.ServicoCatalogoPort.ServicoCatalogo;
import br.com.oficinatrack.ordemservico.application.port.VeiculoConsultaPort;
import br.com.oficinatrack.ordemservico.application.port.VeiculoConsultaPort.VeiculoInfo;
import br.com.oficinatrack.ordemservico.domain.HistoricoStatusOs;
import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import br.com.oficinatrack.ordemservico.domain.OrdemServicoCriada;
import br.com.oficinatrack.ordemservico.domain.StatusOrdemServico;
import br.com.oficinatrack.ordemservico.infra.HistoricoStatusOsRepository;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final HistoricoStatusOsRepository historicoRepository;
    private final ClienteConsultaPort clientePort;
    private final VeiculoConsultaPort veiculoPort;
    private final ServicoCatalogoPort servicoPort;
    private final EstoquePort estoquePort;
    private final ApplicationEventPublisher eventPublisher;

    public OrdemServicoService(OrdemServicoRepository ordemServicoRepository,
                               HistoricoStatusOsRepository historicoRepository,
                               ClienteConsultaPort clientePort,
                               VeiculoConsultaPort veiculoPort,
                               ServicoCatalogoPort servicoPort,
                               EstoquePort estoquePort,
                               ApplicationEventPublisher eventPublisher) {
        this.ordemServicoRepository = ordemServicoRepository;
        this.historicoRepository = historicoRepository;
        this.clientePort = clientePort;
        this.veiculoPort = veiculoPort;
        this.servicoPort = servicoPort;
        this.estoquePort = estoquePort;
        this.eventPublisher = eventPublisher;
    }

    /** HU-5.1 */
    @Transactional
    public OrdemServicoResponse criar(OrdemServicoRequest request) {
        Long clienteId = request.getClienteId();
        Long veiculoId = request.getVeiculoId();

        if (!clientePort.existeAtivo(clienteId)) {
            throw new RegraDeNegocioException("cliente inexistente ou inativo: " + clienteId);
        }

        VeiculoInfo veiculo = veiculoPort.buscar(veiculoId)
                .filter(VeiculoInfo::ativo)
                .orElseThrow(() -> new RegraDeNegocioException("veículo inexistente ou inativo: " + veiculoId));

        if (!veiculo.clienteId().equals(clienteId)) {
            throw new RegraDeNegocioException("o veículo " + veiculoId + " não pertence ao cliente " + clienteId);
        }

        OrdemServico os = ordemServicoRepository.save(OrdemServico.abrir(clienteId, veiculoId));
        historicoRepository.save(new HistoricoStatusOs(os.getId(), null, os.getStatus()));
        eventPublisher.publishEvent(new OrdemServicoCriada(os.getId(), clienteId, veiculoId, os.getCriadoEm()));

        return OrdemServicoResponse.de(os);
    }

    /** HU-5.2 */
    @Transactional
    public OrdemServicoResponse adicionarServico(Long id, ItemServicoRequest request) {
        OrdemServico os = buscarEntidade(id);
        os.exigirAceitaNovosItens();

        // serviço inexistente ou inativo é erro de regra de negócio (400), não 404 da OS
        ServicoCatalogo servico = servicoPort.buscar(request.getServicoId())
                .filter(ServicoCatalogo::ativo)
                .orElseThrow(() -> new RegraDeNegocioException(
                        "serviço inexistente ou inativo: " + request.getServicoId()));

        // valor e descrição são copiados do catálogo neste momento
        os.adicionarServico(servico.id(), servico.nome(), servico.valor(), request.getQuantidade());
        return OrdemServicoResponse.de(ordemServicoRepository.save(os));
    }

    /** HU-5.3 */
    @Transactional
    public OrdemServicoResponse adicionarPeca(Long id, ItemPecaRequest request) {
        OrdemServico os = buscarEntidade(id);
        os.exigirAceitaNovosItens();

        PecaEstoque peca = estoquePort.consultar(request.getPecaId())
                .filter(PecaEstoque::ativa)
                .orElseThrow(() -> new RegraDeNegocioException(
                        "peça inexistente ou inativa: " + request.getPecaId()));

        int totalSolicitado = os.quantidadeDaPeca(peca.id()) + request.getQuantidade();
        if (totalSolicitado > peca.quantidadeDisponivel()) {
            throw new RegraDeNegocioException("estoque insuficiente para a peça '" + peca.nome()
                    + "': disponível " + peca.quantidadeDisponivel() + ", solicitado " + totalSolicitado);
        }

        os.adicionarPeca(peca.id(), peca.nome(), peca.valorUnitario(), request.getQuantidade());
        return OrdemServicoResponse.de(ordemServicoRepository.save(os));
    }

    /** HU-5.5 */
    @Transactional
    public OrdemServicoResponse enviarOrcamento(Long id) {
        OrdemServico os = buscarEntidade(id);

        StatusOrdemServico anterior = os.enviarOrcamento();
        OrdemServico salva = ordemServicoRepository.save(os);
        historicoRepository.save(new HistoricoStatusOs(salva.getId(), anterior, salva.getStatus()));

        return OrdemServicoResponse.de(salva);
    }

    private OrdemServico buscarEntidade(Long id) {
        return ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "ordem de serviço não encontrada com o id: " + id));
    }
}
