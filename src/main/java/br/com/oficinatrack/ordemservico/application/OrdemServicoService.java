package br.com.oficinatrack.ordemservico.application;

import br.com.oficinatrack.ordemservico.api.dto.*;
import br.com.oficinatrack.ordemservico.application.port.*;
import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import br.com.oficinatrack.ordemservico.domain.StatusOrdemServico;
import java.time.LocalDateTime;

@Service
public class OrdemServicoService {
    private final OrdemServicoRepository ordemRepository;
    private final ClienteVeiculoPort clienteVeiculoPort;
    private final CatalogoServicoPort catalogoServicoPort;
    private final EstoquePort estoquePort;

    public OrdemServicoService(OrdemServicoRepository ordemRepository, ClienteVeiculoPort clienteVeiculoPort,
                               CatalogoServicoPort catalogoServicoPort, EstoquePort estoquePort) {
        this.ordemRepository = ordemRepository;
        this.clienteVeiculoPort = clienteVeiculoPort;
        this.catalogoServicoPort = catalogoServicoPort;
        this.estoquePort = estoquePort;
    }

    @Transactional
    public OrdemServicoResponse criar(CriarOrdemServicoRequest request) {
        clienteVeiculoPort.validarVinculo(request.clienteId(), request.veiculoId());
        return OrdemServicoResponse.de(ordemRepository.save(OrdemServico.abrir(request.clienteId(), request.veiculoId())));
    }

    @Transactional
    public OrdemServicoResponse iniciarDiagnostico(Long id) {
        OrdemServico os = buscar(id);
        os.iniciarDiagnostico();
        return responder(os);
    }

    @Transactional
    public OrdemServicoResponse adicionarServico(Long id, AdicionarItemRequest request) {
        OrdemServico os = buscar(id);
        ItemOrcavel servico = catalogoServicoPort.buscarServicoAtivo(request.referenciaId());
        os.adicionarServico(servico.referenciaId(), servico.descricao(), servico.valorUnitario(), request.quantidade());
        return responder(os);
    }

    @Transactional
    public OrdemServicoResponse adicionarPeca(Long id, AdicionarItemRequest request) {
        OrdemServico os = buscar(id);
        ItemOrcavel peca = estoquePort.buscarPecaDisponivel(request.referenciaId(), request.quantidade());
        os.adicionarPeca(peca.referenciaId(), peca.descricao(), peca.valorUnitario(), request.quantidade());
        return responder(os);
    }

    @Transactional
    public OrdemServicoResponse calcularOrcamento(Long id) {
        OrdemServico os = buscar(id);
        os.calcularOrcamento();
        return responder(os);
    }

    @Transactional
    public OrdemServicoResponse enviarOrcamento(Long id, Long orcamentoId) {
        OrdemServico os = buscar(id);
        os.enviarOrcamento(orcamentoId);
        return responder(os);
    }

    @Transactional
    public OrdemServicoResponse finalizar(Long id) {
        OrdemServico os = buscar(id);
        os.finalizarExecucao();
        return responder(os);
    }

    @Transactional
    public OrdemServicoResponse entregar(Long id) {
        OrdemServico os = buscar(id);
        os.confirmarEntrega();
        return responder(os);
    }

    @Transactional
    public OrdemServicoResponse cancelar(Long id, CancelarOrdemServicoRequest request) {
        OrdemServico os = ordemRepository.findByIdParaAtualizacao(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada."));
        os.cancelar(request.motivo());
        return responder(os);
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse detalhar(Long id) { return OrdemServicoResponse.de(buscar(id)); }

    @Transactional(readOnly = true)
    public Page<OrdemServicoResumoResponse> listar(StatusOrdemServico status, Long clienteId, Long veiculoId,
                                                   LocalDateTime inicio, LocalDateTime fim, Pageable pageable) {
        return ordemRepository.buscar(status, clienteId, veiculoId, inicio, fim, pageable)
                .map(OrdemServicoResumoResponse::de);
    }

    private OrdemServico buscar(Long id) {
        return ordemRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada."));
    }

    // grava antes de montar a resposta, para itens, orçamentos e histórico novos já virem com id
    private OrdemServicoResponse responder(OrdemServico os) {
        ordemRepository.flush();
        return OrdemServicoResponse.de(os);
    }
}
