package br.com.oficinatrack.auth.api;

import br.com.oficinatrack.auth.application.JwtService;
import br.com.oficinatrack.auth.api.dto.LoginRequest;
import br.com.oficinatrack.auth.api.dto.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Endpoints para gestão de login e emissão de tokens JWT")
@RestController
@RequestMapping("/auth")
@SecurityRequirements
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Realizar Login", description = "Autentica um utilizador no sistema utilizando credenciais válidas e devolve o token JWT de acesso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login efetuado com sucesso. Token gerado."),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas (Utilizador ou senha incorretos)"),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição ausente ou malformado")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsuario(), request.getSenha())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.gerarToken(userDetails);
        Long expiraEm = jwtService.getExpirationTime();

        return ResponseEntity.ok(new TokenResponse(token, expiraEm));
    }
}
