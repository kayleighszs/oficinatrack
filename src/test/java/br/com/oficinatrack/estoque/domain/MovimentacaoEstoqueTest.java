package br.com.oficinatrack.estoque.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MovimentacaoEstoque")
class MovimentacaoEstoqueTest {

    private final Peca peca = Peca.cadastrar("Motor", BigDecimal.TEN, 10, 5);

    @Nested
    @DisplayName("registrarManual")
    class RegistrarManual {

        @Test
        @DisplayName("deve criar movimentação com origem MANUAL e ordemServicoId nulo")
        void deveCriarComOrigemManualEOrdemServicoNula() {
            MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarManual(
                    peca, TipoMovimentacao.ENTRADA, 5, 1L
            );

            assertThat(movimentacao.getOrigem()).isEqualTo(Origem.MANUAL);
            assertThat(movimentacao.getOrdemServicoId()).isNull();
        }

        @Test
        @DisplayName("deve manter o tipo, a quantidade e o usuarioId informados")
        void deveManterTipoQuantidadeEUsuarioInformados() {
            MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarManual(
                    peca, TipoMovimentacao.SAIDA, 3, 7L
            );

            assertThat(movimentacao.getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
            assertThat(movimentacao.getQuantidade()).isEqualTo(3);
            assertThat(movimentacao.getUsuarioId()).isEqualTo(7L);
            assertThat(movimentacao.getPeca()).isEqualTo(peca);
        }

        @Test
        @DisplayName("deve preencher criadoEm automaticamente")
        void devePreencherCriadoEmAutomaticamente() {
            MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarManual(
                    peca, TipoMovimentacao.ENTRADA, 5, 1L
            );

            assertThat(movimentacao.getCriadoEm()).isNotNull();
        }

        @Test
        @DisplayName("deve aceitar tanto ENTRADA quanto SAIDA como tipo, refletindo o que foi informado")
        void deveAceitarAmbosOsTiposConformeInformado() {
            MovimentacaoEstoque entrada = MovimentacaoEstoque.registrarManual(
                    peca, TipoMovimentacao.ENTRADA, 5, 1L
            );
            MovimentacaoEstoque saida = MovimentacaoEstoque.registrarManual(
                    peca, TipoMovimentacao.SAIDA, 5, 1L
            );

            assertThat(entrada.getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);
            assertThat(saida.getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
        }
    }

    @Nested
    @DisplayName("registrarAutomatica")
    class RegistrarAutomatica {

        @Test
        @DisplayName("deve criar movimentação com origem BAIXA_OS e usuarioId nulo")
        void deveCriarComOrigemBaixaOsEUsuarioNulo() {
            MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarAutomatica(
                    peca, 4, 100L
            );

            assertThat(movimentacao.getOrigem()).isEqualTo(Origem.BAIXA_OS);
            assertThat(movimentacao.getUsuarioId()).isNull();
        }

        @Test
        @DisplayName("deve sempre definir o tipo como SAIDA, independente de outros valores")
        void deveSempreDefinirTipoComoSaida() {
            MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarAutomatica(
                    peca, 4, 100L
            );

            assertThat(movimentacao.getTipo()).isEqualTo(TipoMovimentacao.SAIDA);
        }

        @Test
        @DisplayName("deve preencher ordemServicoId, quantidade e a referência da peça corretamente")
        void devePreencherOrdemServicoQuantidadeEPeca() {
            MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarAutomatica(
                    peca, 4, 100L
            );

            assertThat(movimentacao.getOrdemServicoId()).isEqualTo(100L);
            assertThat(movimentacao.getQuantidade()).isEqualTo(4);
            assertThat(movimentacao.getPeca()).isEqualTo(peca);
        }

        @Test
        @DisplayName("deve preencher criadoEm automaticamente")
        void devePreencherCriadoEmAutomaticamente() {
            MovimentacaoEstoque movimentacao = MovimentacaoEstoque.registrarAutomatica(
                    peca, 4, 100L
            );

            assertThat(movimentacao.getCriadoEm()).isNotNull();
        }
    }
}