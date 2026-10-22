package br.com.oficinatrack.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfCnpjValidator implements ConstraintValidator<CpfCnpj, String> {

    // nulo/vazio é válido por convenção do Bean Validation; obrigatoriedade fica com @NotBlank
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return DocumentoFiscal.isValido(value);
    }
}
