package br.com.oficinatrack.auth.infra;

import br.com.oficinatrack.auth.domain.Role;
import br.com.oficinatrack.auth.domain.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                                   Environment environment) {
        return args -> {
            provisionar("GESTOR", Role.GESTOR, usuarioRepository, passwordEncoder, environment);
            provisionar("ATENDENTE", Role.ATENDENTE, usuarioRepository, passwordEncoder, environment);
            provisionar("MECANICO", Role.MECANICO, usuarioRepository, passwordEncoder, environment);
        };
    }

    private void provisionar(String prefixo, Role role, UsuarioRepository repository,
                             PasswordEncoder encoder, Environment environment) {
        String username = environment.getProperty(prefixo + "_USERNAME");
        String password = environment.getProperty(prefixo + "_PASSWORD");
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }
        if (repository.findByUsername(username).isEmpty()) {
            repository.save(new Usuario(username, encoder.encode(password), role));
        }
    }
}
