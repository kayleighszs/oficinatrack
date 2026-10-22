package br.com.oficinatrack.veiculo.domain;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlacaTest {

    @Test
    void aceitaPadraoAntigoENormaliza() {
        Placa placa = new Placa(" abc-1234 ");
        assertEquals("ABC1234", placa.getValue());
        assertFalse(placa.isMercosul());
    }

    @Test
    void aceitaPadraoMercosul() {
        Placa placa = new Placa("abc 1d23");
        assertEquals("ABC1D23", placa.getValue());
        assertTrue(placa.isMercosul());
    }

    @ParameterizedTest
    @ValueSource(strings = {"AB1234", "ABCD123", "1234ABC", "ABC12345", "ABC1D2E", "ABC.1234", "ÁBC1234"})
    void rejeitaPlacaInvalida(String valor) {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class, () -> new Placa(valor));
        assertTrue(erro.getMessage().contains("AAA9A99"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void rejeitaPlacaNulaOuVazia(String valor) {
        assertThrows(RegraDeNegocioException.class, () -> new Placa(null));
        assertThrows(RegraDeNegocioException.class, () -> new Placa(valor));
    }

    @ParameterizedTest
    @CsvSource({"ABC1234, ABC1C34", "ABC1034, ABC1A34", "ABC1934, ABC1J34", "ABC1C34, ABC1234"})
    void converteEntreOsPadroes(String origem, String esperada) {
        assertEquals(Optional.of(new Placa(esperada)), new Placa(origem).equivalente());
    }

    @Test
    void mercosulComLetraAposJNaoTemEquivalenteAntiga() {
        Placa placa = new Placa("ABC1K34");
        assertTrue(placa.equivalente().isEmpty());
        assertEquals(List.of("ABC1K34"), placa.formasEquivalentes());
    }

    @Test
    void identificaMesmoVeiculoAntesEDepoisDaConversao() {
        assertTrue(new Placa("ABC1234").mesmoVeiculoQue(new Placa("ABC-1C34")));
        assertFalse(new Placa("ABC1234").mesmoVeiculoQue(new Placa("ABC1D34")));
    }
}
