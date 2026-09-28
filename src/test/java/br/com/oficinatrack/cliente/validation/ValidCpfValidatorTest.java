package br.com.oficinatrack.cliente.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidCpfValidatorTest {

    private final ValidCpfValidator validator = new ValidCpfValidator();

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", "11222333000181", "11.222.333/0001-81"})
    void deveAceitarCpfECnpjValidos(String documento) {
        assertTrue(validator.isValid(documento, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224724",        // dígito verificador do CPF errado
            "11222333000182",     // dígito verificador do CNPJ errado
            "11111111111",        // CPF com dígitos repetidos
            "00000000000000",     // CNPJ com dígitos repetidos
            "123",                // tamanho inválido
            "123456789012",       // 12 dígitos
            "abcdefghijk"         // sem dígitos
    })
    void deveRejeitarDocumentosInvalidos(String documento) {
        assertFalse(validator.isValid(documento, null));
    }

    @Test
    void deveRejeitarValorQueNaoESring() {
        assertFalse(validator.isValid(12345678909L, null));
    }

    @Test
    void deveAceitarNuloEVazioPoisObrigatoriedadeEDeOutraAnotacao() {
        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("   ", null));
    }
}
