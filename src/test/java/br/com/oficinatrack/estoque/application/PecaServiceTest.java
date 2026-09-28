package br.com.oficinatrack.estoque.application;

import br.com.oficinatrack.estoque.application.dto.MovimentacaoEstoqueRequest;
import br.com.oficinatrack.estoque.application.dto.PecaRequest;
import br.com.oficinatrack.estoque.application.dto.PecaResponse;
import br.com.oficinatrack.estoque.application.dto.PecaUpdateRequest;
import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.domain.TipoMovimentacao;
import br.com.oficinatrack.estoque.exceptions.EstoqueInsuficienteExcepetion;
import br.com.oficinatrack.estoque.exceptions.PecaNaoEncontradaException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("PecaService")
class PecaServiceTest {

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @InjectMocks
    private PecaService pecaService;

    @Nested
    @DisplayName("cadastrar")
    class Cadastrar {

        @Test
        @DisplayName("deve criar a peça com os dados do request e delegar para o repository")
        void deveCadastrarPecaEDelegarParaRepository() {
            PecaRequest request = mock(PecaRequest.class);
            when(request.getNome()).thenReturn("Motor");
            when(request.getValorUnitario()).thenReturn(BigDecimal.TEN);
            when(request.getQuantidadeEstoque()).thenReturn(10);
            when(request.getEstoqueMinimo()).thenReturn(5);

            when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

            PecaResponse response = pecaService.cadastrar(request);

            assertThat(response).isNotNull();

            ArgumentCaptor<Peca> captor = ArgumentCaptor.forClass(Peca.class);
            verify(pecaRepository).save(captor.capture());
            assertThat(captor.getValue().getNome()).isEqualTo("Motor");
            assertThat(captor.getValue().getQuantidadeEstoque()).isEqualTo(10);
            assertThat(captor.getValue().getEstoqueMinimo()).isEqualTo(5);
        }

        @Test
        @DisplayName("deve propagar IllegalArgumentException quando valorUnitario é inválido, sem salvar")
        void devePropagarExcecaoQuandoValorUnitarioInvalido() {
            PecaRequest request = mock(PecaRequest.class);
            when(request.getNome()).thenReturn("Motor");
            when(request.getValorUnitario()).thenReturn(BigDecimal.ZERO);
            when(request.getQuantidadeEstoque()).thenReturn(10);
            when(request.getEstoqueMinimo()).thenReturn(5);

            assertThatThrownBy(() -> pecaService.cadastrar(request))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(pecaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("buscarPorId")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar PecaResponse quando a peça existe")
        void deveRetornarPecaResponseQuandoExiste() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

            PecaResponse response = pecaService.buscarPorId(1L);

            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("deve lançar PecaNaoEncontradaException quando a peça não existe")
        void deveLancarExcecaoQuandoNaoExiste() {
            when(pecaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pecaService.buscarPorId(99L))
                    .isInstanceOf(PecaNaoEncontradaException.class);
        }
    }

    @Nested
    @DisplayName("listar")
    class Listar {

        @Test
        @DisplayName("deve delegar para buscarComFiltros e mapear o resultado para PecaResponse")
        void deveDelegarParaBuscarComFiltrosEMapearResultado() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Peca> page = new PageImpl<>(List.of(peca));

            when(pecaRepository.buscarComFiltros("Motor", false, pageable)).thenReturn(page);

            Page<PecaResponse> resultado = pecaService.listar("Motor", false, pageable);

            assertThat(resultado.getTotalElements()).isEqualTo(1);
            verify(pecaRepository).buscarComFiltros("Motor", false, pageable);
        }
    }

    @Nested
    @DisplayName("atualizar")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar dados cadastrais sem alterar quantidadeEstoque")
        void deveAtualizarDadosCadastraisSemAlterarQuantidadeEstoque() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
            when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

            PecaUpdateRequest request = mock(PecaUpdateRequest.class);
            when(request.getNome()).thenReturn("Motor V2");
            when(request.getValorUnitario()).thenReturn(BigDecimal.valueOf(20));
            when(request.getEstoqueMinimo()).thenReturn(8);

            pecaService.atualizar(1L, request);

            assertThat(peca.getNome()).isEqualTo("Motor V2");
            assertThat(peca.getValorUnitario()).isEqualByComparingTo(BigDecimal.valueOf(20));
            assertThat(peca.getEstoqueMinimo()).isEqualTo(8);
            assertThat(peca.getQuantidadeEstoque()).isEqualTo(10);
        }

        @Test
        @DisplayName("deve lançar PecaNaoEncontradaException quando a peça não existe, sem salvar")
        void deveLancarExcecaoQuandoPecaNaoExisteParaAtualizar() {
            when(pecaRepository.findById(99L)).thenReturn(Optional.empty());
            PecaUpdateRequest request = mock(PecaUpdateRequest.class);

            assertThatThrownBy(() -> pecaService.atualizar(99L, request))
                    .isInstanceOf(PecaNaoEncontradaException.class);

            verify(pecaRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("movimentarEstoque")
    class MovimentarEstoque {

        @Test
        @DisplayName("deve incrementar o estoque quando o tipo é ENTRADA")
        void deveIncrementarEstoqueQuandoTipoEntrada() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
            when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

            MovimentacaoEstoqueRequest request = new MovimentacaoEstoqueRequest(TipoMovimentacao.ENTRADA, 5);

            pecaService.movimentarEstoque(1L, request);

            assertThat(peca.getQuantidadeEstoque()).isEqualTo(15);
            verify(movimentacaoEstoqueRepository).save(any(MovimentacaoEstoque.class));
        }

        @Test
        @DisplayName("deve debitar o estoque quando o tipo é SAIDA")
        void deveDebitarEstoqueQuandoTipoSaida() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
            when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

            MovimentacaoEstoqueRequest request = new MovimentacaoEstoqueRequest(TipoMovimentacao.SAIDA, 4);

            pecaService.movimentarEstoque(1L, request);

            assertThat(peca.getQuantidadeEstoque()).isEqualTo(6);
        }

        @Test
        @DisplayName("deve registrar a movimentação com origem MANUAL e o usuarioId hardcoded")
        void deveRegistrarMovimentacaoComOrigemManual() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
            when(pecaRepository.save(any(Peca.class))).thenAnswer(inv -> inv.getArgument(0));

            MovimentacaoEstoqueRequest request = new MovimentacaoEstoqueRequest(TipoMovimentacao.ENTRADA, 5);

            pecaService.movimentarEstoque(1L, request);

            ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
            verify(movimentacaoEstoqueRepository).save(captor.capture());
            assertThat(captor.getValue().getOrigem()).isEqualTo(br.com.oficinatrack.estoque.domain.Origem.MANUAL);
            assertThat(captor.getValue().getUsuarioId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve lançar EstoqueInsuficienteExcepetion quando a saída deixaria estoque negativo, sem registrar movimentação")
        void deveLancarEstoqueInsuficienteQuandoSaidaExcedeDisponivel() {
            Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 2, 5);
            when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

            MovimentacaoEstoqueRequest request = new MovimentacaoEstoqueRequest(TipoMovimentacao.SAIDA, 10);

            assertThatThrownBy(() -> pecaService.movimentarEstoque(1L, request))
                    .isInstanceOf(EstoqueInsuficienteExcepetion.class);

            verify(pecaRepository, never()).save(any());
            verify(movimentacaoEstoqueRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar PecaNaoEncontradaException quando a peça não existe, sem registrar movimentação")
        void deveLancarPecaNaoEncontradaQuandoPecaNaoExisteParaMovimentar() {
            when(pecaRepository.findById(99L)).thenReturn(Optional.empty());

            MovimentacaoEstoqueRequest request = new MovimentacaoEstoqueRequest(TipoMovimentacao.ENTRADA, 5);

            assertThatThrownBy(() -> pecaService.movimentarEstoque(99L, request))
                    .isInstanceOf(PecaNaoEncontradaException.class);

            verify(movimentacaoEstoqueRepository, never()).save(any());
        }
    }
}