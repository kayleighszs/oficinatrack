package br.com.oficinatrack.publico.application;

import br.com.oficinatrack.estoque.domain.*;
import br.com.oficinatrack.estoque.infrastructure.*;
import br.com.oficinatrack.ordemservico.api.dto.OrdemServicoResponse;
import br.com.oficinatrack.ordemservico.domain.*;
import br.com.oficinatrack.ordemservico.infra.OrdemServicoRepository;
import br.com.oficinatrack.publico.api.dto.ConsultaPublicaResponse.*;
import br.com.oficinatrack.publico.api.dto.DecisaoOrcamentoRequest;
import br.com.oficinatrack.shared.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConsultaPublicaService {
    private final AcessoPublicoService acessoService;
    private final OrdemServicoRepository ordemRepository;
    private final PecaRepository pecaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    public ConsultaPublicaService(AcessoPublicoService acessoService, OrdemServicoRepository ordemRepository,
                                  PecaRepository pecaRepository,
                                  MovimentacaoEstoqueRepository movimentacaoRepository) {
        this.acessoService = acessoService;
        this.ordemRepository = ordemRepository;
        this.pecaRepository = pecaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @Transactional(readOnly = true)
    public Status consultarStatus(String authorization, String numeroOs) {
        OrdemServico os = acessoService.validar(authorization, numeroOs).getOrdemServico();
        Long orcamentoAtual = os.getOrcamentos().stream()
                .filter(o -> o.getStatus() == StatusOrcamento.ENVIADO || o.getStatus() == StatusOrcamento.APROVADO
                        || o.getStatus() == StatusOrcamento.RECUSADO)
                .reduce((a, b) -> b).map(Orcamento::getId).orElse(null);
        return new Status(os.getNumeroOs(), os.getStatus(), orcamentoAtual);
    }

    @Transactional(readOnly = true)
    public OrcamentoPublico consultarOrcamento(String authorization, String numeroOs, Long orcamentoId) {
        OrdemServico os = acessoService.validar(authorization, numeroOs).getOrdemServico();
        Orcamento orcamento = localizarPublico(os, orcamentoId);
        return respostaOrcamento(os, orcamento);
    }

    @Transactional
    public OrdemServicoResponse decidir(String authorization, String numeroOs, Long orcamentoId,
                                        DecisaoOrcamentoRequest request) {
        Long ordemId = acessoService.validar(authorization, numeroOs).getOrdemServico().getId();
        OrdemServico os = ordemRepository.findByNumeroOsParaAtualizacao(numeroOs)
                .filter(o -> o.getId().equals(ordemId))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento não encontrado."));
        Orcamento orcamento = localizarPublico(os, orcamentoId);

        if (request.decisao() == DecisaoOrcamentoRequest.Decisao.APROVADO) {
            if (orcamento.getStatus() == StatusOrcamento.APROVADO) return OrdemServicoResponse.de(os);
            if (orcamento.getStatus() != StatusOrcamento.ENVIADO)
                throw new ConflitoException("Orçamento já recebeu uma decisão diferente.");
            baixarEstoque(os, orcamento);
            os.aprovarOrcamento(orcamento);
        } else {
            if (orcamento.getStatus() == StatusOrcamento.RECUSADO) return OrdemServicoResponse.de(os);
            if (orcamento.getStatus() != StatusOrcamento.ENVIADO)
                throw new ConflitoException("Orçamento já recebeu uma decisão diferente.");
            os.recusarOrcamento(orcamento, request.motivo());
        }
        return OrdemServicoResponse.de(os);
    }

    private void baixarEstoque(OrdemServico os, Orcamento orcamento) {
        List<Long> ids = os.getItensPeca().stream().map(ItemPecaOrdem::getPecaId).sorted().toList();
        if (ids.isEmpty()) return;
        Map<Long, Peca> pecas = pecaRepository.buscarTodasParaAtualizacao(ids).stream()
                .collect(Collectors.toMap(Peca::getId, Function.identity()));
        if (pecas.size() != ids.size()) throw new ConflitoException("Uma peça do orçamento não existe mais.");

        for (ItemPecaOrdem item : os.getItensPeca()) {
            Peca peca = pecas.get(item.getPecaId());
            if (!Boolean.TRUE.equals(peca.getAtivo()))
                throw new ConflitoException("Peça inativa não pode ser baixada: " + peca.getNome());
            peca.debitarEstoque(item.getQuantidade());
            movimentacaoRepository.save(MovimentacaoEstoque.registrarAutomatica(
                    peca, item.getQuantidade(), os.getId(), orcamento.getId()));
        }
    }

    private Orcamento localizarPublico(OrdemServico os, Long id) {
        Orcamento orcamento = os.getOrcamentos().stream().filter(o -> Objects.equals(o.getId(), id)).findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento não encontrado."));
        if (orcamento.getStatus() == StatusOrcamento.CALCULADO || orcamento.getStatus() == StatusOrcamento.SUBSTITUIDO)
            throw new RecursoNaoEncontradoException("Orçamento não encontrado.");
        return orcamento;
    }

    private OrcamentoPublico respostaOrcamento(OrdemServico os, Orcamento orcamento) {
        List<Item> servicos = os.getItensServico().stream().map(i ->
                new Item(i.getDescricao(), i.getValorUnitario(), i.getQuantidade(), i.subtotal())).toList();
        List<Item> pecas = os.getItensPeca().stream().map(i ->
                new Item(i.getDescricao(), i.getValorUnitario(), i.getQuantidade(), i.subtotal())).toList();
        return new OrcamentoPublico(orcamento.getId(), orcamento.getVersao(), orcamento.getTotal(),
                orcamento.getStatus(), servicos, pecas);
    }
}
