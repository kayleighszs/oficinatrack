package br.com.oficinatrack.auth.api;

import br.com.oficinatrack.auth.api.dto.CriarUsuarioRequest;
import br.com.oficinatrack.auth.domain.Usuario;
import br.com.oficinatrack.auth.infra.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Utilizadores Internos", description = "Endpoints para gestão de acessos e operadores do sistema")
@RestController
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "Cadastrar novo utilizador", description = "Cria um novo operador (ATENDENTE) ou administrador (ADMIN). Ação exclusiva para utilizadores com perfil ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Utilizador cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou utilizador já existente"),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas ADMIN pode realizar esta ação")
    })
    @PostMapping
    public ResponseEntity<Void> cadastrarUsuario(@Valid @RequestBody CriarUsuarioRequest request) {
        if (usuarioRepository.findByUsername(request.getUsuario()).isPresent()) {
            throw new RegraDeNegocioException("Já existe um usuário cadastrado com este nome de login.");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(request.getUsuario());
        novoUsuario.setSenha(passwordEncoder.encode(request.getSenha()));
        novoUsuario.setRole(request.getRole());

        usuarioRepository.save(novoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}