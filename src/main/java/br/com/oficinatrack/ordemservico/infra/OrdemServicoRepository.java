package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    Optional<OrdemServico> findByNumeroOs(String numeroOs);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrdemServico o WHERE o.numeroOs = :numeroOs")
    Optional<OrdemServico> findByNumeroOsParaAtualizacao(@Param("numeroOs") String numeroOs);
}
