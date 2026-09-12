package br.com.oficinatrack.cliente.domain;

public class CpfCnpj {
    private final String value;

    public CpfCnpj(String value) {
        // validation placeholder
        this.value = value;
    }

    public String getValue() { return value; }
}
