package br.com.oficinatrack.ordemservico.infra.acl;

import br.com.oficinatrack.catalogoservicos.infra.ServicoRepository;
import br.com.oficinatrack.ordemservico.application.port.CatalogoServicoPort;
import br.com.oficinatrack.ordemservico.application.port.ItemOrcavel;
import br.com.oficinatrack.shared.exception.RegraDeNegocioException;
import org.springframework.stereotype.Component;

@Component
class CatalogoServicoAdapter implements CatalogoServicoPort {
    private final ServicoRepository servicoRepository;

    CatalogoServicoAdapter(ServicoRepository servicoRepository) { this.servicoRepository = servicoRepository; }

    @Override
    public ItemOrcavel buscarServicoAtivo(Long servicoId) {
        return servicoRepository.findById(servicoId)
                .filter(s -> Boolean.TRUE.equals(s.getAtivo()))
                .map(s -> new ItemOrcavel(s.getId(), s.getNome(), s.getValor()))
                .orElseThrow(() -> new RegraDeNegocioException("Serviço inexistente ou inativo."));
    }
}
