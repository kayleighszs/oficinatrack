package br.com.oficinatrack.shared.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfCnpjValidatorTest {

    private final CpfCnpjValidator validator = new CpfCnpjValidator();

    @ParameterizedTest
    @ValueSource(strings = {"529.982.247-25", "11.222.333/0001-81", "12.ABC.345/01DE-35"})
    void deveAceitarCpfECnpjValidos(String documento) {
        assertTrue(validator.isValid(documento, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"52998224724", "11222333000182", "111.111.111-11"})
    void deveRejeitarDocumentosInvalidos(String documento) {
        assertFalse(validator.isValid(documento, null));
    }

    @Test
    void deveAceitarNuloEVazioPoisObrigatoriedadeEDeOutraAnotacao() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("   ", null));
    }
}
