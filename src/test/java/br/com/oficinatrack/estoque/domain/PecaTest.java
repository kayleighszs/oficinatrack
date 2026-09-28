package br.com.oficinatrack.estoque.domain;

import br.com.oficinatrack.estoque.exceptions.EstoqueInsuficienteExcepetion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class PecaTest {

    @Nested
    @DisplayName("Peca.cadastrar()")
    class Cadastrar {

        @Test
        @DisplayName("deve criar peça com dados válidos")
        void deveCriarPecaComDadosValidos() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThat(peca.getNome()).isEqualTo("Parafuso");
            assertThat(peca.getValorUnitario()).isEqualByComparingTo(BigDecimal.valueOf(2.50));
            assertThat(peca.getQuantidadeEstoque()).isEqualTo(100);
            assertThat(peca.getEstoqueMinimo()).isEqualTo(10);
        }

        @Test
        @DisplayName("deve iniciar como ativa por padrão")
        void deveIniciarAtiva() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThat(peca.getAtivo()).isTrue();
        }

        @Test
        @DisplayName("deve preencher criadoEm e atualizadoEm")
        void devePreencherDatas() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThat(peca.getCriadoEm()).isNotNull();
            assertThat(peca.getAtualizadoEm()).isNotNull();
        }

        @Test
        @DisplayName("deve lançar exceção quando valorUnitario for nulo")
        void deveLancarExcecaoValorUnitarioNulo() {
            assertThatThrownBy(() -> Peca.cadastrar("Parafuso", null, 100, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando valorUnitario for zero")
        void deveLancarExcecaoValorUnitarioZero() {
            assertThatThrownBy(() -> Peca.cadastrar("Parafuso", BigDecimal.ZERO, 100, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando valorUnitario for negativo")
        void deveLancarExcecaoValorUnitarioNegativo() {
            assertThatThrownBy(() -> Peca.cadastrar("Parafuso", BigDecimal.valueOf(-1), 100, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidadeEstoque for nula")
        void deveLancarExcecaoQuantidadeEstoqueNula() {
            assertThatThrownBy(() -> Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), null, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidadeEstoque for negativa")
        void deveLancarExcecaoQuantidadeEstoqueNegativa() {
            assertThatThrownBy(() -> Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), -1, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("não deve lançar exceção quando quantidadeEstoque for zero")
        void naoDeveLancarExcecaoQuantidadeEstoqueZero() {
            assertThatCode(() -> Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 0, 10))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("deve lançar exceção quando estoqueMinimo for nulo")
        void deveLancarExcecaoEstoqueMinimoNulo() {
            assertThatThrownBy(() -> Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando estoqueMinimo for negativo")
        void deveLancarExcecaoEstoqueMinimoNegativo() {
            assertThatThrownBy(() -> Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando nome for nulo")
        void deveLancarExcecaoNomeNulo() {
            assertThatThrownBy(() -> Peca.cadastrar(null, BigDecimal.valueOf(2.50), 100, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando nome for vazio ou em branco")
        void deveLancarExcecaoNomeEmBranco() {
            assertThatThrownBy(() -> Peca.cadastrar("   ", BigDecimal.valueOf(2.50), 100, 10))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("debitarEstoque()")
    class DebitarEstoque {

        @Test
        @DisplayName("deve diminuir o estoque quando quantidade for menor que o disponível")
        void deveDiminuirEstoque() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            peca.debitarEstoque(30);

            assertThat(peca.getQuantidadeEstoque()).isEqualTo(70);
        }

        @Test
        @DisplayName("deve permitir debitar quantidade igual ao estoque disponível, zerando-o")
        void devePermitirZerarEstoque() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            peca.debitarEstoque(100);

            assertThat(peca.getQuantidadeEstoque()).isZero();
        }

        @Test
        @DisplayName("deve lançar EstoqueInsuficienteExcepetion quando quantidade for maior que o disponível")
        void deveLancarExcecaoQuandoEstoqueInsuficiente() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.debitarEstoque(101))
                    .isInstanceOf(EstoqueInsuficienteExcepetion.class);
        }

        @Test
        @DisplayName("não deve alterar o estoque quando a operação falhar por insuficiência")
        void naoDeveAlterarEstoqueQuandoFalhar() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.debitarEstoque(101))
                    .isInstanceOf(EstoqueInsuficienteExcepetion.class);

            assertThat(peca.getQuantidadeEstoque()).isEqualTo(100);
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidade for nula")
        void deveLancarExcecaoQuandoQuantidadeNula() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.debitarEstoque(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidade for zero")
        void deveLancarExcecaoQuandoQuantidadeZero() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.debitarEstoque(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidade for negativa")
        void deveLancarExcecaoQuandoQuantidadeNegativa() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.debitarEstoque(-5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("incrementarEstoque()")
    class IncrementarEstoque {

        @Test
        @DisplayName("deve aumentar o estoque com quantidade válida")
        void deveAumentarEstoque() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            peca.incrementarEstoque(50);

            assertThat(peca.getQuantidadeEstoque()).isEqualTo(150);
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidade for nula")
        void deveLancarExcecaoQuandoQuantidadeNula() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.incrementarEstoque(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidade for zero")
        void deveLancarExcecaoQuandoQuantidadeZero() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.incrementarEstoque(0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando quantidade for negativa")
        void deveLancarExcecaoQuandoQuantidadeNegativa() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.incrementarEstoque(-10))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("atualizarDadosCadastrais()")
    class AtualizarDadosCadastrais {

        @Test
        @DisplayName("deve atualizar nome, valorUnitario e estoqueMinimo")
        void deveAtualizarDados() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            peca.atualizarDadosCadastrais("Parafuso Sextavado", BigDecimal.valueOf(3.00), 20);

            assertThat(peca.getNome()).isEqualTo("Parafuso Sextavado");
            assertThat(peca.getValorUnitario()).isEqualByComparingTo(BigDecimal.valueOf(3.00));
            assertThat(peca.getEstoqueMinimo()).isEqualTo(20);
        }

        @Test
        @DisplayName("não deve alterar a quantidadeEstoque")
        void naoDeveAlterarQuantidadeEstoque() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            peca.atualizarDadosCadastrais("Parafuso Sextavado", BigDecimal.valueOf(3.00), 20);

            assertThat(peca.getQuantidadeEstoque()).isEqualTo(100);
        }

        @Test
        @DisplayName("deve atualizar o atualizadoEm para um instante mais recente")
        void deveAtualizarAtualizadoEm() throws InterruptedException {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);
            LocalDateTime atualizadoEmOriginal = peca.getAtualizadoEm();

            Thread.sleep(5); // garante diferença perceptível de timestamp
            peca.atualizarDadosCadastrais("Parafuso Sextavado", BigDecimal.valueOf(3.00), 20);

            assertThat(peca.getAtualizadoEm()).isAfter(atualizadoEmOriginal);
        }

        @Test
        @DisplayName("deve lançar exceção quando valorUnitario for inválido")
        void deveLancarExcecaoValorUnitarioInvalido() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.atualizarDadosCadastrais("Novo Nome", BigDecimal.ZERO, 20))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando estoqueMinimo for inválido")
        void deveLancarExcecaoEstoqueMinimoInvalido() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.atualizarDadosCadastrais("Novo Nome", BigDecimal.valueOf(3.00), -1))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve lançar exceção quando nome for inválido")
        void deveLancarExcecaoNomeInvalido() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.atualizarDadosCadastrais("", BigDecimal.valueOf(3.00), 20))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("não deve alterar nenhum campo quando a validação falhar")
        void naoDeveAlterarCamposQuandoFalhar() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 100, 10);

            assertThatThrownBy(() -> peca.atualizarDadosCadastrais("Nome Novo", BigDecimal.ZERO, 20))
                    .isInstanceOf(IllegalArgumentException.class);

            assertThat(peca.getNome()).isEqualTo("Parafuso");
            assertThat(peca.getValorUnitario()).isEqualByComparingTo(BigDecimal.valueOf(2.50));
            assertThat(peca.getEstoqueMinimo()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("isEstoqueBaixo()")
    class IsEstoqueBaixo {

        @Test
        @DisplayName("deve retornar true quando quantidadeEstoque for menor que estoqueMinimo")
        void deveRetornarTrueQuandoMenor() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 5, 10);

            assertThat(peca.isEstoqueBaixo()).isTrue();
        }

        @Test
        @DisplayName("deve retornar true quando quantidadeEstoque for igual a estoqueMinimo")
        void deveRetornarTrueQuandoIgual() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 10, 10);

            assertThat(peca.isEstoqueBaixo()).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando quantidadeEstoque for maior que estoqueMinimo")
        void deveRetornarFalseQuandoMaior() {
            Peca peca = Peca.cadastrar("Parafuso", BigDecimal.valueOf(2.50), 50, 10);

            assertThat(peca.isEstoqueBaixo()).isFalse();
        }
    }
}