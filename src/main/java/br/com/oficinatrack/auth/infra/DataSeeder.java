package br.com.oficinatrack.auth.infra;

import br.com.oficinatrack.auth.domain.Role;
import br.com.oficinatrack.auth.domain.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                                   Environment environment) {
        return args -> {
            try {
                log.info("Iniciando o seeder de usuários de demonstração...");
                provisionar("GESTOR", Role.GESTOR, "gestor", "Senha@123", usuarioRepository, passwordEncoder, environment);
                provisionar("ATENDENTE", Role.ATENDENTE, "atendente", "Senha@123", usuarioRepository, passwordEncoder, environment);
                provisionar("MECANICO", Role.MECANICO, "mecanico", "Senha@123", usuarioRepository, passwordEncoder, environment);
            } catch (Exception e) {
                log.error("Erro durante a execução do DataSeeder: {}", e.getMessage(), e);
            }
        };
    }

    @Transactional
    protected void provisionar(String prefixo, Role role, String defaultUser, String defaultPass,
                               UsuarioRepository repository, PasswordEncoder encoder, Environment environment) {
        // Tenta ler a variável de ambiente. Se não existir, utiliza o valor default fornecido
        String username = environment.getProperty(prefixo + "_USERNAME", defaultUser);
        String password = environment.getProperty(prefixo + "_PASSWORD", defaultPass);

        if (repository.findByUsername(username).isEmpty()) {
            Usuario novoUsuario = new Usuario(username, encoder.encode(password), role);
            repository.save(novoUsuario);
            log.info("[SEED OK] Usuário criado: {} | Perfil: {}", username, role);
        } else {
            log.info("[SEED SKIP] Usuário '{}' já está cadastrado no banco.", username);
        }
    }
}
