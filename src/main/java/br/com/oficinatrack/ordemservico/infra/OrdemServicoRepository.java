package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    Optional<OrdemServico> findByNumeroOs(String numeroOs);
}
