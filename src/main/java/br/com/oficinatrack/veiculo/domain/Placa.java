package br.com.oficinatrack.veiculo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Placa {
    @Column(name = "placa", length = 10)
    private String value;

    protected Placa() { this.value = null; }

    public Placa(String value) {
        // validation placeholder
        this.value = value;
    }

    public String getValue() { return value; }
}
