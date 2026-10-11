package br.com.oficinatrack.auth.infra;

import br.com.oficinatrack.auth.domain.Role;
import br.com.oficinatrack.auth.domain.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setSenha(passwordEncoder.encode("123456"));
                admin.setRole(Role.ADMIN);

                usuarioRepository.save(admin);
                System.out.println(">>> [SECURITY] Usuário ADMIN padrão criado automaticamente com sucesso!");
            }
        };
    }
}