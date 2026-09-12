package br.com.oficinatrack.veiculo.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "veiculos")
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String modelo;
    @Embedded
    private Placa placa;

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public String getModelo() {return modelo;}
    public void setModelo(String modelo) {this.modelo = modelo;}
    public Placa getPlaca() {return placa;}
    public void setPlaca(Placa placa) {this.placa = placa;}
}
