package br.com.oficinatrack.ordemservico.infra.acl;

import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.infrastructure.MovimentacaoEstoqueRepository;
import br.com.oficinatrack.estoque.infrastructure.PecaRepository;
import br.com.oficinatrack.ordemservico.application.port.EstoquePort;
import br.com.oficinatrack.ordemservico.application.port.ItemOrcavel;
import br.com.oficinatrack.shared.exception.ConflitoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class EstoqueAdapter implements EstoquePort {
    private final PecaRepository pecaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;

    EstoqueAdapter(PecaRepository pecaRepository, MovimentacaoEstoqueRepository movimentacaoRepository) {
        this.pecaRepository = pecaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    @Override
    public ItemOrcavel buscarPecaDisponivel(Long pecaId, int quantidade) {
        Peca peca = pecaRepository.findById(pecaId)
                .filter(p -> Boolean.TRUE.equals(p.getAtivo()))
                .orElseThrow(() -> new RegraDeNegocioException("Peça inexistente ou inativa."));
        if (peca.getQuantidadeEstoque() < quantidade)
            throw new ConflitoException("Peça indisponível no estoque: " + peca.getNome()
                    + ". Disponível: " + peca.getQuantidadeEstoque() + ", solicitado: " + quantidade + ".");
        return new ItemOrcavel(peca.getId(), peca.getNome(), peca.getValorUnitario());
    }

    @Override
    public void baixar(Long ordemServicoId, Long orcamentoId, List<ConsumoPeca> consumos) {
        List<Long> ids = consumos.stream().map(ConsumoPeca::pecaId).sorted().toList();
        if (ids.isEmpty()) return;
        Map<Long, Peca> pecas = pecaRepository.buscarTodasParaAtualizacao(ids).stream()
                .collect(Collectors.toMap(Peca::getId, Function.identity()));
        if (pecas.size() != ids.size()) throw new ConflitoException("Uma peça do orçamento não existe mais.");

        for (ConsumoPeca consumo : consumos) {
            Peca peca = pecas.get(consumo.pecaId());
            if (!Boolean.TRUE.equals(peca.getAtivo()))
                throw new ConflitoException("Peça inativa não pode ser baixada: " + peca.getNome());
            peca.debitarEstoque(consumo.quantidade());
            movimentacaoRepository.save(MovimentacaoEstoque.registrarAutomatica(
                    peca, consumo.quantidade(), ordemServicoId, orcamentoId));
        }
    }
}
