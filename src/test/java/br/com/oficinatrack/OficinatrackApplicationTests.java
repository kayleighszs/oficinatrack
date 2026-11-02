package br.com.oficinatrack;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "api.security.token.secret=segredo-de-testes-com-trinta-e-dois-caracteres")
class OficinatrackApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void deveAplicarMigrationsECarregarContexto() {
        assertNotNull(applicationContext, "O contexto Spring deve ser inicializado com sucesso");
    }
}
