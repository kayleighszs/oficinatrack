package br.com.oficinatrack.cliente.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class CpfCnpj {
    @Column(name = "cpf_cnpj", length = 20)
    private String value;

    protected CpfCnpj() { this.value = null; }

    public CpfCnpj(String value) {
        // validation placeholder
        this.value = value;
    }

    public String getValue() { return value; }
}
