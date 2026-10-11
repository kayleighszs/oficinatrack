package br.com.oficinatrack.auth.api.dto;

import br.com.oficinatrack.auth.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CriarUsuarioRequest {

    @NotBlank(message = "O nome de usuário é obrigatório")
    private String usuario;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    @NotNull(message = "O perfil (role) é obrigatório")
    private Role role; // ADMIN ou ATENDENTE

    // Getters e Setters
    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}