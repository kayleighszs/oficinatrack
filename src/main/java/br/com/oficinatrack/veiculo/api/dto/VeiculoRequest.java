package br.com.oficinatrack.veiculo.api.dto;

public class VeiculoRequest {
    private String modelo;
    private String placa;

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
}
