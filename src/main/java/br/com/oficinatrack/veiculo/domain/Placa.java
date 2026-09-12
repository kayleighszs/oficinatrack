package br.com.oficinatrack.veiculo.domain;

public class Placa {
    private final String value;

    public Placa(String value) {
        // validation placeholder
        this.value = value;
    }

    public String getValue() { return value; }
}
