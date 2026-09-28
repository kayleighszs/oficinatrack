package br.com.oficinatrack.veiculo.domain;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.regex.Pattern;

@Embeddable
public class Placa {
    // Padrão antigo (AAA9999) ou Mercosul (AAA9A99)
    private static final Pattern FORMATO = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    @Column(name = "placa", length = 10, unique = true, nullable = false)
    private String value;

    protected Placa() { this.value = null; }

    public Placa(String value) {
        if (value == null) {
            throw new RegraDeNegocioException("Placa é obrigatória");
        }
        String normalizada = value.toUpperCase().replaceAll("[\\s-]", "");
        if (!FORMATO.matcher(normalizada).matches()) {
            throw new RegraDeNegocioException("Placa inválida: " + value);
        }
        this.value = normalizada;
    }

    public String getValue() { return value; }
}
