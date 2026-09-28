package br.com.oficinatrack.veiculo.domain;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlacaTest {

    @Test
    void aceitaPadraoAntigoENormaliza() {
        assertEquals("ABC1234", new Placa("abc-1234").getValue());
    }

    @Test
    void aceitaPadraoMercosul() {
        assertEquals("ABC1D23", new Placa("ABC1D23").getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "AB1234", "ABCD123", "1234ABC", "ABC12345"})
    void rejeitaPlacaInvalida(String valor) {
        assertThrows(RegraDeNegocioException.class, () -> new Placa(valor));
    }

    @Test
    void rejeitaPlacaNula() {
        assertThrows(RegraDeNegocioException.class, () -> new Placa(null));
    }
}
