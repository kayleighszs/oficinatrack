package br.com.oficinatrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "api.security.token.secret=segredo-de-testes-com-trinta-e-dois-caracteres")
class OficinatrackApplicationTests {

    @Test
    void deveAplicarMigrationsECarregarContexto() {
    }
}
