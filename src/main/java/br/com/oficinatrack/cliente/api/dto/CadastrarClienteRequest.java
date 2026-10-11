package br.com.oficinatrack.cliente.api.dto;

import br.com.oficinatrack.cliente.validation.ValidCpf;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CadastrarClienteRequest {

    @NotBlank(message = "Nome do cliente é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String nome;

    @NotBlank(message = "CPF/CNPJ é obrigatório")
    @ValidCpf(message = "CPF/CNPJ inválido")
    private String cpfCnpj;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    @Pattern(regexp = "^[0-9\\s\\-()+]*$", message = "telefone inválido")
    private String telefone;

    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
    @Email(message = "e-mail inválido")
    private String email;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
