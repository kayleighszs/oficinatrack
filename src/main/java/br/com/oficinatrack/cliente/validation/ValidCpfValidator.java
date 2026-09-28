package br.com.oficinatrack.cliente.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidCpfValidator implements ConstraintValidator<ValidCpf, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        if (!(value instanceof String)) return false;

        String documento = ((String) value).trim();
        if (documento.isEmpty()) return true;

        documento = documento.replaceAll("\\D", "");
        if (documento.chars().distinct().count() == 1) return false;

        return switch (documento.length()) {
            case 11 -> cpfValido(documento);
            case 14 -> cnpjValido(documento);
            default -> false;
        };
    }

    private boolean cpfValido(String cpf) {
        int primeiro = digitoVerificador(cpf, 9, 10);
        int segundo = digitoVerificador(cpf, 10, 11);
        return primeiro == digito(cpf, 9) && segundo == digito(cpf, 10);
    }

    private boolean cnpjValido(String cnpj) {
        int[] pesos1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesos2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        return calcularCnpj(cnpj, pesos1) == digito(cnpj, 12)
                && calcularCnpj(cnpj, pesos2) == digito(cnpj, 13);
    }

    private int digitoVerificador(String numero, int quantidade, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += digito(numero, i) * (pesoInicial - i);
        }
        int resto = 11 - (soma % 11);
        return resto >= 10 ? 0 : resto;
    }

    private int calcularCnpj(String cnpj, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += digito(cnpj, i) * pesos[i];
        }
        int resto = 11 - (soma % 11);
        return resto >= 10 ? 0 : resto;
    }

    private int digito(String numero, int posicao) {
        return Character.getNumericValue(numero.charAt(posicao));
    }
}
