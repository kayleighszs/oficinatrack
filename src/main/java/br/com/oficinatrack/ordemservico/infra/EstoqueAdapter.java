package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.estoque.application.PecaService;
import br.com.oficinatrack.estoque.application.dto.PecaResponse;
import br.com.oficinatrack.ordemservico.application.port.EstoquePort;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EstoqueAdapter implements EstoquePort {

    private final PecaService pecaService;

    public EstoqueAdapter(PecaService pecaService) {
        this.pecaService = pecaService;
    }

    @Override
    public Optional<PecaEstoque> consultar(Long pecaId) {
        try {
            PecaResponse peca = pecaService.buscarPorId(pecaId);
            return Optional.of(new PecaEstoque(
                    peca.id(),
                    peca.nome(),
                    peca.valorUnitario(),
                    peca.quantidadeEstoque(),
                    Boolean.TRUE.equals(peca.ativo())));
        } catch (RecursoNaoEncontradoException ex) {
            return Optional.empty();
        }
    }
}
