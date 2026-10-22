package br.com.oficinatrack.veiculo.domain;

import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Embeddable
public class Placa {
    private static final Pattern FORMATO_ANTIGO = Pattern.compile("^[A-Z]{3}[0-9]{4}$");
    private static final Pattern FORMATO_MERCOSUL = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
    private static final Pattern SEPARADORES = Pattern.compile("[\\s-]");

    // na conversão para Mercosul o 2º número vira letra: 0->A, 1->B, ..., 9->J
    private static final int POSICAO_CONVERTIDA = 4;

    @Column(name = "placa", length = 10, unique = true, nullable = false)
    private String value;

    protected Placa() { this.value = null; }

    public Placa(String value) {
        if (value == null || value.isBlank()) {
            throw new RegraDeNegocioException("Placa é obrigatória");
        }
        String normalizada = SEPARADORES.matcher(value).replaceAll("").toUpperCase(Locale.ROOT);
        if (!FORMATO_ANTIGO.matcher(normalizada).matches() && !FORMATO_MERCOSUL.matcher(normalizada).matches()) {
            throw new RegraDeNegocioException(
                    "Placa inválida: " + value + ". Formatos aceitos: AAA9999 (antigo) ou AAA9A99 (Mercosul)");
        }
        this.value = normalizada;
    }

    public String getValue() { return value; }

    public boolean isMercosul() {
        return FORMATO_MERCOSUL.matcher(value).matches();
    }

    /** Mesma placa no outro padrão (ABC1234 <-> ABC1C34); vazio para Mercosul sem correspondente antigo (letra após J). */
    public Optional<Placa> equivalente() {
        char c = value.charAt(POSICAO_CONVERTIDA);
        char convertido;
        if (Character.isDigit(c)) {
            convertido = (char) ('A' + (c - '0'));
        } else if (c <= 'J') {
            convertido = (char) ('0' + (c - 'A'));
        } else {
            return Optional.empty();
        }
        StringBuilder outra = new StringBuilder(value);
        outra.setCharAt(POSICAO_CONVERTIDA, convertido);
        return Optional.of(new Placa(outra.toString()));
    }

    /** A própria placa e a do outro padrão, se houver; usado para checar duplicidade. */
    public List<String> formasEquivalentes() {
        return equivalente()
                .map(outra -> List.of(value, outra.value))
                .orElseGet(() -> List.of(value));
    }

    public boolean mesmoVeiculoQue(Placa outra) {
        return outra != null && formasEquivalentes().contains(outra.value);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Placa outra && Objects.equals(value, outra.value));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
