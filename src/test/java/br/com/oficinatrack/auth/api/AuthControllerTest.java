package br.com.oficinatrack.auth.api;

import br.com.oficinatrack.auth.api.dto.LoginRequest;
import br.com.oficinatrack.auth.api.dto.TokenResponse;
import br.com.oficinatrack.auth.application.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    @Test
    void deveRealizarLoginComSucessoERetornarToken() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsuario("admin");
        request.setSenha("123456");

        Authentication authenticationMock = mock(Authentication.class);
        UserDetails userDetailsMock = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);

        when(authenticationMock.getPrincipal()).thenReturn(userDetailsMock);

        when(jwtService.gerarToken(userDetailsMock)).thenReturn("meu.token.jwt");
        when(jwtService.getExpirationTime()).thenReturn(3600000L);

        ResponseEntity<TokenResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("meu.token.jwt", response.getBody().getToken());
        assertEquals(3600000L, response.getBody().getExpiraEm());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtService, times(1)).gerarToken(userDetailsMock);
    }
}