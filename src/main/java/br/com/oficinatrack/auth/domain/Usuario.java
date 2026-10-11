package br.com.oficinatrack.auth.domain;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios") // Nome da tabela separado para não misturar com clientes
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username; // Campo de login (pode ser email ou username conforme o seu requisito)

    @Column(nullable = false)
    private String senha; // Guardada encriptada com BCrypt

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // ADMIN ou ATENDENTE

    public Usuario() {
    }

    public Usuario(String username, String senha, Role role) {
        this.username = username;
        this.senha = senha;
        this.role = role;
    }

    // --- Métodos do Spring Security (UserDetails) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (this.role == Role.ADMIN) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_ATENDENTE")
            );
        }
        return List.of(new SimpleGrantedAuthority("ROLE_ATENDENTE"));
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String getPassword() {
        return this.senha;
    }

}