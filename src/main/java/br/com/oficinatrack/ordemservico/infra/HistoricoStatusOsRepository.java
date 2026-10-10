package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.ordemservico.domain.HistoricoStatusOs;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoStatusOsRepository extends JpaRepository<HistoricoStatusOs, Long> {
}
