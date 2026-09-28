package br.com.oficinatrack.veiculo.domain;

import br.com.oficinatrack.cliente.domain.Cliente;
import jakarta.persistence.*;

@Entity
@Table(name = "veiculos")
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String marca;
    private String modelo;
    private Integer ano;
    @Embedded
    private Placa placa;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    @Column(nullable = false)
    private boolean ativo = true;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getMarca() {return marca;}
    public void setMarca(String marca) {this.marca = marca;}
    public String getModelo() {return modelo;}
    public void setModelo(String modelo) {this.modelo = modelo;}
    public Integer getAno() {return ano;}
    public void setAno(Integer ano) {this.ano = ano;}
    public Placa getPlaca() {return placa;}
    public void setPlaca(Placa placa) {this.placa = placa;}
    public Cliente getCliente() {return cliente;}
    public void setCliente(Cliente cliente) {this.cliente = cliente;}
    public boolean isAtivo() {return ativo;}
    public void setAtivo(boolean ativo) {this.ativo = ativo;}
}
