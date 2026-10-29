package br.com.oficinatrack.ordemservico.infra.acl;

import br.com.oficinatrack.estoque.domain.Peca;
import br.com.oficinatrack.estoque.infrastructure.MovimentacaoEstoqueRepository;
import br.com.oficinatrack.estoque.infrastructure.PecaRepository;
import br.com.oficinatrack.ordemservico.application.port.ItemOrcavel;
import br.com.oficinatrack.shared.exception.ConflitoException;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstoqueAdapterTest {
    @Mock PecaRepository pecaRepository;
    @Mock MovimentacaoEstoqueRepository movimentacaoRepository;
    @InjectMocks EstoqueAdapter adapter;

    private Peca peca(int estoque) {
        Peca peca = Peca.cadastrar("Filtro", new BigDecimal("20.00"), estoque, 1);
        ReflectionTestUtils.setField(peca, "id", 4L);
        return peca;
    }

    @Test
    void deveTraduzirPecaDisponivelParaItemDaOrdem() {
        when(pecaRepository.findById(4L)).thenReturn(Optional.of(peca(5)));

        ItemOrcavel item = adapter.buscarPecaDisponivel(4L, 5);

        assertEquals(new ItemOrcavel(4L, "Filtro", new BigDecimal("20.00")), item);
    }

    @Test
    void deveRecusarPecaComEstoqueInsuficiente() {
        when(pecaRepository.findById(4L)).thenReturn(Optional.of(peca(2)));

        ConflitoException erro = assertThrows(ConflitoException.class, () -> adapter.buscarPecaDisponivel(4L, 3));
        assertTrue(erro.getMessage().contains("indisponível"));
    }

    @Test
    void deveRecusarPecaInativaOuInexistente() {
        Peca inativa = peca(10);
        ReflectionTestUtils.setField(inativa, "ativo", false);
        when(pecaRepository.findById(4L)).thenReturn(Optional.of(inativa));
        when(pecaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(RegraDeNegocioException.class, () -> adapter.buscarPecaDisponivel(4L, 1));
        assertThrows(RegraDeNegocioException.class, () -> adapter.buscarPecaDisponivel(5L, 1));
    }
}
