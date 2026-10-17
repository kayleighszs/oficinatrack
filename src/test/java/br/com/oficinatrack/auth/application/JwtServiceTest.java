package br.com.oficinatrack.auth.application;

import br.com.oficinatrack.auth.domain.Role;
import br.com.oficinatrack.auth.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "minhaChaveSecretaMuitoLongaParaTestesUnitarios123456");
        ReflectionTestUtils.setField(jwtService, "expirationTime", 1_800_000L);
    }

    @Test
    void deveGerarTokenValidoParaUsuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("joao_teste");
        usuario.setRole(Role.ATENDENTE);

        String token = jwtService.gerarToken(usuario);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deveExtrairUsernameDoTokenCorretamente() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin_teste");
        usuario.setRole(Role.GESTOR);
        String token = jwtService.gerarToken(usuario);

        String usernameExtraido = jwtService.extrairUsername(token);

        assertEquals("admin_teste", usernameExtraido);
    }
}
