package br.com.oficinatrack.veiculo.api.dto;

import jakarta.validation.constraints.Positive;

/**
 * Atualização parcial: somente os campos enviados (não nulos) são alterados.
 * Enviar {"ativo": false} inativa o veículo.
 */
public class VeiculoUpdateRequest {
    private String placa;
    private String marca;
    private String modelo;
    @Positive(message = "ano deve ser positivo")
    private Integer ano;
    private Boolean ativo;

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getAno() { return ano; }
    public void setAno(Integer ano) { this.ano = ano; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }
}
