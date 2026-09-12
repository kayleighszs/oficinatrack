package br.com.oficinatrack.cliente.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    @Embedded
    private CpfCnpj cpfCnpj;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public CpfCnpj getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(CpfCnpj cpfCnpj) { this.cpfCnpj = cpfCnpj; }
}
