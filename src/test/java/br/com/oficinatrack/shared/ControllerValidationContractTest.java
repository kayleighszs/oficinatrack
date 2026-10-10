package br.com.oficinatrack.shared;

import br.com.oficinatrack.cliente.api.ClienteController;
import br.com.oficinatrack.cliente.application.ClienteService;
import br.com.oficinatrack.ordemservico.api.OrdemServicoController;
import br.com.oficinatrack.ordemservico.application.OrdemServicoService;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.Validation;
import jakarta.validation.executable.ExecutableValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControllerValidationContractTest {

    private final ExecutableValidator validator = Validation.buildDefaultValidatorFactory()
            .getValidator()
            .forExecutables();

    @Test
    void ordemServicoControllerNaoDeveRedefinirRestricoesDaInterface() {
        OrdemServicoController controller = new OrdemServicoController(Mockito.mock(OrdemServicoService.class));

        validarTodosOsEndpoints(controller);
    }

    @Test
    void clienteControllerNaoDeveRedefinirRestricoesDaInterface() {
        ClienteController controller = new ClienteController(Mockito.mock(ClienteService.class));

        validarTodosOsEndpoints(controller);
    }

    @Test
    void veiculoControllerNaoDeveRedefinirRestricoesDaInterface() {
        validarTodosOsEndpoints(new br.com.oficinatrack.veiculo.api.VeiculoController(
                Mockito.mock(br.com.oficinatrack.veiculo.application.VeiculoService.class)));
    }

    @Test
    void pecaControllerNaoDeveRedefinirRestricoesDaInterface() {
        validarTodosOsEndpoints(new br.com.oficinatrack.estoque.api.PecaController(
                Mockito.mock(br.com.oficinatrack.estoque.application.PecaService.class)));
    }

    @Test
    void servicoControllerNaoDeveRedefinirRestricoesDaInterface() {
        validarTodosOsEndpoints(new br.com.oficinatrack.catalogoservicos.api.ServicoController(
                Mockito.mock(br.com.oficinatrack.catalogoservicos.application.ServicoService.class)));
    }

    private void validarTodosOsEndpoints(Object controller) {
        int validados = 0;
        for (Method method : controller.getClass().getDeclaredMethods()) {
            if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic()) {
                continue;
            }
            Object[] args = argumentosPadrao(method);
            assertDoesNotThrow(() -> {
                try {
                    validator.validateParameters(controller, method, args);
                } catch (ConstraintDeclarationException ex) {
                    throw new AssertionError("Configuração de validação inválida em "
                            + method.getName() + ": " + ex.getMessage(), ex);
                }
            });
            validados++;
        }
        assertTrue(validados > 0, "nenhum endpoint foi validado");
    }

    private Object[] argumentosPadrao(Method method) {
        Class<?>[] tipos = method.getParameterTypes();
        Object[] args = new Object[tipos.length];
        for (int i = 0; i < tipos.length; i++) {
            if (tipos[i] == Long.class) {
                args[i] = 1L;
            } else if (tipos[i] == String.class) {
                args[i] = null;
            } else {
                try {
                    args[i] = tipos[i].getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException ex) {
                    args[i] = null;
                }
            }
        }
        return args;
    }
}
