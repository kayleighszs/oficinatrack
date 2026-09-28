package br.com.oficinatrack.estoque.application;

import br.com.oficinatrack.estoque.application.events.OrcamentoAprovadoEvent;
import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.infrastructure.MovimentacaoEstoqueRepository;
import br.com.oficinatrack.estoque.infrastructure.PecaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ATENÇÃO: os construtores de OrcamentoAprovadoEvent e OrcamentoAprovadoEvent.ItemPecaAprovado
 * foram assumidos como records (ordemServicoId, itens) e (pecaId, quantidade), respectivamente.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EstoqueEventListener")
class EstoqueEventListenerTest {

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @InjectMocks
    private EstoqueEventListener listener;

    @Nested
    @DisplayName("aoAprovarOrcamento - cenário de sucesso")
    class CenarioDeSucesso {

        @Test
        @DisplayName("deve debitar estoque e registrar movimentação automática quando a peça existe e tem estoque suficiente")
        void deveDebitarEstoqueERegistrarMovimentacaoAutomatica() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
            when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

            var item = new OrcamentoAprovadoEvent.ItemPecaAprovado(1L, 3);
            var event = new OrcamentoAprovadoEvent(100L, List.of(item));

            listener.aoAprovarOrcamento(event);

            assertThat(peca.getQuantidadeEstoque()).isEqualTo(7);

            ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
            verify(movimentacaoEstoqueRepository).save(captor.capture());
            assertThat(captor.getValue().getOrdemServicoId()).isEqualTo(100L);
            assertThat(captor.getValue().getQuantidade()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("aoAprovarOrcamento - estoque insuficiente")
    class EstoqueInsuficiente {

        @Test
        @DisplayName("não deve registrar movimentação e não deve propagar exceção quando o estoque é insuficiente")
        void naoDeveRegistrarMovimentacaoQuandoEstoqueInsuficiente() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 2, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

            var item = new OrcamentoAprovadoEvent.ItemPecaAprovado(1L, 10);
            var event = new OrcamentoAprovadoEvent(100L, List.of(item));

            assertThatCode(() -> listener.aoAprovarOrcamento(event)).doesNotThrowAnyException();

            verify(movimentacaoEstoqueRepository, never()).save(any());
            assertThat(peca.getQuantidadeEstoque()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("aoAprovarOrcamento - peça não encontrada")
    class PecaNaoEncontrada {

        @Test
        @DisplayName("não deve registrar movimentação e não deve propagar exceção quando a peça não existe mais")
        void naoDeveRegistrarMovimentacaoQuandoPecaNaoExiste() {
            when(pecaRepository.findById(99L)).thenReturn(Optional.empty());

            var item = new OrcamentoAprovadoEvent.ItemPecaAprovado(99L, 5);
            var event = new OrcamentoAprovadoEvent(100L, List.of(item));

            assertThatCode(() -> listener.aoAprovarOrcamento(event)).doesNotThrowAnyException();

            verify(movimentacaoEstoqueRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("aoAprovarOrcamento - erro inesperado")
    class ErroInesperado {

        @Test
        @DisplayName("não deve propagar exceção quando ocorre uma falha técnica não mapeada")
        void naoDevePropagarExcecaoQuandoOcorreErroTecnico() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
            when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));
            when(movimentacaoEstoqueRepository.save(any()))
                    .thenThrow(new RuntimeException("Falha de conexão simulada"));

            var item = new OrcamentoAprovadoEvent.ItemPecaAprovado(1L, 3);
            var event = new OrcamentoAprovadoEvent(100L, List.of(item));

            assertThatCode(() -> listener.aoAprovarOrcamento(event)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("aoAprovarOrcamento - múltiplos itens")
    class MultiplosItens {

        @Test
        @DisplayName("deve continuar processando os itens seguintes quando um item anterior falha")
        void deveContinuarProcessandoOutrosItensQuandoUmFalha() {
            Peca pecaOk = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);

            when(pecaRepository.findById(1L)).thenReturn(Optional.empty());
            when(pecaRepository.findById(2L)).thenReturn(Optional.of(pecaOk));
            when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

            var itemComFalha = new OrcamentoAprovadoEvent.ItemPecaAprovado(1L, 5);
            var itemOk = new OrcamentoAprovadoEvent.ItemPecaAprovado(2L, 3);
            var event = new OrcamentoAprovadoEvent(100L, List.of(itemComFalha, itemOk));

            listener.aoAprovarOrcamento(event);

            assertThat(pecaOk.getQuantidadeEstoque()).isEqualTo(7);
            verify(movimentacaoEstoqueRepository, times(1)).save(any());
        }
    }
}