package br.com.oficinatrack.shared.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentoFiscalTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", " 529 982 247 25 "})
    void deveReconhecerCpfValido(String cpf) {
        assertTrue(DocumentoFiscal.isCpf(cpf));
        assertFalse(DocumentoFiscal.isCnpj(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000181", "11.222.333/0001-81", "12ABC34501DE35", "12.ABC.345/01DE-35", "12.abc.345/01de-35"})
    void deveReconhecerCnpjNumericoEAlfanumericoValido(String cnpj) {
        assertTrue(DocumentoFiscal.isCnpj(cnpj));
        assertFalse(DocumentoFiscal.isCpf(cnpj));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224724",        // DV do CPF errado
            "11222333000182",     // DV do CNPJ errado
            "12ABC34501DE36",     // DV do CNPJ alfanumérico errado
            "12ABC34501DEAB",     // DV do CNPJ não pode ser letra
            "5299822472A",        // CPF não aceita letra
            "11111111111",        // CPF com dígitos repetidos
            "00000000000000",     // CNPJ com dígitos repetidos
            "123",                // tamanho inválido
            "123456789012",       // 12 caracteres
            "529*982*247*25",     // caractere fora da máscara
            "abcdefghijk",
            ""
    })
    void deveRejeitarDocumentosInvalidos(String documento) {
        assertFalse(DocumentoFiscal.isValido(documento));
    }

    @Test
    void deveRejeitarNulo() {
        assertFalse(DocumentoFiscal.isValido(null));
    }

    @Test
    void normalizarDeveRemoverMascaraEColocarEmMaiusculas() {
        assertEquals("52998224725", DocumentoFiscal.normalizar("529.982.247-25"));
        assertEquals("12ABC34501DE35", DocumentoFiscal.normalizar("12.abc.345/01de-35"));
        assertNull(DocumentoFiscal.normalizar(null));
    }
}
