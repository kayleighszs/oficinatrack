package br.com.oficinatrack.cliente.api.dto;

public class ClienteRequest {
    private String nome;
    private String cpfCnpj;

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }
}
