package br.com.oficinatrack.auth.application;

import br.com.oficinatrack.auth.domain.Role;
import br.com.oficinatrack.auth.domain.Usuario;
import br.com.oficinatrack.auth.infra.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void deveRetornarUserDetailsQuandoUsuarioExiste() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setUsername("admin");
        usuarioMock.setSenha("123456");
        usuarioMock.setRole(Role.ADMIN);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioMock));

        UserDetails resultado = userDetailsService.loadUserByUsername("admin");

        assertNotNull(resultado);
        assertEquals("admin", resultado.getUsername());
        assertEquals("123456", resultado.getPassword());
        verify(usuarioRepository, times(1)).findByUsername("admin");
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        when(usuarioRepository.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("fantasma");
        });
        verify(usuarioRepository, times(1)).findByUsername("fantasma");
    }
}