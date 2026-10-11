package br.com.oficinatrack.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfCnpjValidator implements ConstraintValidator<CpfCnpj, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        String documento = value.replaceAll("\\D", "");

        if (documento.length() == 11) {
            return validarCpf(documento);
        } else if (documento.length() == 14) {
            return validarCnpj(documento);
        }
        return false;
    }

    private boolean validarCpf(String cpf) {
        if (cpf.matches("(\\d)\\1{10}")) return false;
        try {
            int soma = 0, resto;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            resto = 11 - (soma % 11);
            int digito1 = (resto == 10 || resto == 11) ? 0 : resto;

            if (digito1 != Character.getNumericValue(cpf.charAt(9))) return false;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            resto = 11 - (soma % 11);
            int digito2 = (resto == 10 || resto == 11) ? 0 : resto;

            return digito2 == Character.getNumericValue(cpf.charAt(10));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validarCnpj(String cnpj) {
        if (cnpj.matches("(\\d)\\1{13}")) return false;
        try {
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso1[i];
            }
            int resto = soma % 11;
            int digito1 = (resto < 2) ? 0 : (11 - resto);

            if (digito1 != Character.getNumericValue(cnpj.charAt(12))) return false;

            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso2[i];
            }
            resto = soma % 11;
            int digito2 = (resto < 2) ? 0 : (11 - resto);

            return digito2 == Character.getNumericValue(cnpj.charAt(13));
        } catch (Exception e) {
            return false;
        }
    }
}
