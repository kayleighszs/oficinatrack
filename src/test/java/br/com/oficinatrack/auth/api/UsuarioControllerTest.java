package br.com.oficinatrack.auth.api;

import br.com.oficinatrack.auth.api.dto.CriarUsuarioRequest;
import br.com.oficinatrack.auth.domain.Role;
import br.com.oficinatrack.auth.domain.Usuario;
import br.com.oficinatrack.auth.infra.UsuarioRepository;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioController usuarioController;

    @Test
    void deveCadastrarUsuarioComSucesso() {
        // Arrange
        CriarUsuarioRequest request = new CriarUsuarioRequest();
        request.setUsuario("novo_atendente");
        request.setSenha("senhaSegura");
        request.setRole(Role.ATENDENTE);

        when(usuarioRepository.findByUsername("novo_atendente")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senhaSegura")).thenReturn("senhaCriptografada");

        // Act
        ResponseEntity<Void> response = usuarioController.cadastrarUsuario(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        // Verifica se o objeto salvo tem os dados corretos usando um ArgumentCaptor
        ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(usuarioCaptor.capture());

        Usuario usuarioSalvo = usuarioCaptor.getValue();
        assertEquals("novo_atendente", usuarioSalvo.getUsername());
        assertEquals("senhaCriptografada", usuarioSalvo.getSenha());
        assertEquals(Role.ATENDENTE, usuarioSalvo.getRole());
    }

    @Test
    void deveLancarRegraDeNegocioExceptionQuandoUsuarioJaExistir() {
        CriarUsuarioRequest request = new CriarUsuarioRequest();
        request.setUsuario("admin_existente");

        Usuario usuarioExistente = new Usuario();
        when(usuarioRepository.findByUsername("admin_existente")).thenReturn(Optional.of(usuarioExistente));

        RegraDeNegocioException exception = assertThrows(RegraDeNegocioException.class, () -> {
            usuarioController.cadastrarUsuario(request);
        });

        assertEquals("Já existe um usuário cadastrado com este nome de login.", exception.getMessage());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}