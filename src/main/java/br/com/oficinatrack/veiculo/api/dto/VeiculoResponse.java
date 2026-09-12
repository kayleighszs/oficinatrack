package br.com.oficinatrack.veiculo.api.dto;

public class VeiculoResponse {
    private Long id;
    private String modelo;
    private String placa;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
}
