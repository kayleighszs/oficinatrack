package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
}
