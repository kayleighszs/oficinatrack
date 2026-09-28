package br.com.oficinatrack.estoque.application;

import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.exceptions.EstoqueInsuficienteExcepetion;
import br.com.oficinatrack.estoque.exceptions.PecaNaoEncontradaException;
import br.com.oficinatrack.estoque.infrastructure.MovimentacaoEstoqueRepository;
import br.com.oficinatrack.estoque.infrastructure.PecaRepository;
import br.com.oficinatrack.estoque.application.events.OrcamentoAprovadoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class EstoqueEventListener {

    private static final Logger log = LoggerFactory.getLogger(EstoqueEventListener.class);

    private final PecaRepository pecaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public EstoqueEventListener(PecaRepository pecaRepository, MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.pecaRepository = pecaRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoAprovarOrcamento(OrcamentoAprovadoEvent event){
        for (OrcamentoAprovadoEvent.ItemPecaAprovado item : event.itens()){
            processarItem(event.ordemServicoId(), item);
        }
    }

    private void processarItem(Long ordemServicoId, OrcamentoAprovadoEvent.ItemPecaAprovado item){
        try{
           debitarEDescontarEmTransacao(ordemServicoId, item);
        }catch (EstoqueInsuficienteExcepetion ex) {
            log.warn("Estoque insuficiente ao baixar peca {} da OS {}: {}",
                    item.pecaId(), ordemServicoId, ex.getMessage());

            Peca peca = pecaRepository.findById(item.pecaId()).orElse(null);
            Integer disponivel = peca != null ? peca.getQuantidadeEstoque() : 0;

            registrarInconsistencia(item, ordemServicoId, disponivel, ex.getMessage());
        }catch (PecaNaoEncontradaException ex){
            log.warn("Peça {} não encontrada ao processar baixa da OS {}: {}",
                    item.pecaId(), ordemServicoId, ex.getMessage());

            registrarInconsistencia(item, ordemServicoId, 0, ex.getMessage());
        }catch (Exception ex){
            log.error("Erro inesperado ao processar baixa da peça {} da OS {}: {}",
                    item.pecaId(), ordemServicoId, ex.getMessage(), ex);

            registrarInconsistencia(item, ordemServicoId, 0,
                    "Erro inesperado: " + ex.getMessage());
        }
    }

    @Transactional
    public void debitarEDescontarEmTransacao(Long ordemServicoId, OrcamentoAprovadoEvent.ItemPecaAprovado item){
        Peca peca = pecaRepository.findById(item.pecaId())
                .orElseThrow(() -> new PecaNaoEncontradaException(
                        "Peça não encontrada com o ID: " + item.pecaId()
                ));

        peca.debitarEstoque(item.quantidade());
        Peca pecaAtualizada = pecaRepository.save(peca);

        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarAutomatica(
                pecaAtualizada, item.quantidade(), ordemServicoId
        );
        movimentacaoEstoqueRepository.save(movimentacao);
    }

    private void registrarInconsistencia(OrcamentoAprovadoEvent.ItemPecaAprovado item, Long ordemServicoId, Integer quantidadeDisponivel, String motivo){
        log.error("Inconsistência de estoque — peça {}, OS {}, solicitado {}, disponível {}, motivo: {}",
                item.pecaId(), ordemServicoId, item.quantidade(), quantidadeDisponivel, motivo);
    }
}
