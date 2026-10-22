package br.com.oficinatrack.shared.validation;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Regras de CPF e CNPJ num único lugar: normalização e dígitos verificadores.
 * Suporta o CNPJ alfanumérico (IN RFB nº 2.229/2024), em que as 12 primeiras
 * posições podem conter letras e os 2 dígitos verificadores continuam numéricos.
 */
public final class DocumentoFiscal {

    public static final int TAMANHO_CPF = 11;
    public static final int TAMANHO_CNPJ = 14;

    // pontuação aceita em máscaras: 529.982.247-25 / 12.ABC.345/01DE-35
    private static final Pattern MASCARA = Pattern.compile("[.\\-/\\s]");
    private static final Pattern FORMATO_CPF = Pattern.compile("\\d{11}");
    private static final Pattern FORMATO_CNPJ = Pattern.compile("[0-9A-Z]{12}\\d{2}");

    private static final int[] PESOS_CNPJ_DV1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_DV2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private DocumentoFiscal() {
    }

    /** Remove a máscara e coloca em maiúsculas; não descarta caracteres inesperados. */
    public static String normalizar(String valor) {
        if (valor == null) {
            return null;
        }
        return MASCARA.matcher(valor).replaceAll("").toUpperCase(Locale.ROOT);
    }

    public static boolean isValido(String valor) {
        return isCpf(valor) || isCnpj(valor);
    }

    public static boolean isCpf(String valor) {
        String cpf = normalizar(valor);
        if (cpf == null || !FORMATO_CPF.matcher(cpf).matches() || todosIguais(cpf)) {
            return false;
        }
        return digitoCpf(cpf, 9) == valorDe(cpf.charAt(9))
                && digitoCpf(cpf, 10) == valorDe(cpf.charAt(10));
    }

    public static boolean isCnpj(String valor) {
        String cnpj = normalizar(valor);
        if (cnpj == null || !FORMATO_CNPJ.matcher(cnpj).matches() || todosIguais(cnpj)) {
            return false;
        }
        return digitoCnpj(cnpj, PESOS_CNPJ_DV1) == valorDe(cnpj.charAt(12))
                && digitoCnpj(cnpj, PESOS_CNPJ_DV2) == valorDe(cnpj.charAt(13));
    }

    private static int digitoCpf(String cpf, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += valorDe(cpf.charAt(i)) * (quantidade + 1 - i);
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static int digitoCnpj(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += valorDe(cnpj.charAt(i)) * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    // regra da Receita: valor = código ASCII - 48 ('0'..'9' -> 0..9, 'A'..'Z' -> 17..42)
    private static int valorDe(char c) {
        return c - '0';
    }

    private static boolean todosIguais(String valor) {
        return valor.chars().distinct().count() == 1;
    }
}
