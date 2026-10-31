package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import br.com.oficinatrack.ordemservico.domain.StatusOrdemServico;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
    Optional<OrdemServico> findByNumeroOs(String numeroOs);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrdemServico o WHERE o.numeroOs = :numeroOs")
    Optional<OrdemServico> findByNumeroOsParaAtualizacao(@Param("numeroOs") String numeroOs);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrdemServico o WHERE o.id = :id")
    Optional<OrdemServico> findByIdParaAtualizacao(@Param("id") Long id);

    @Query("""
            SELECT o FROM OrdemServico o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:clienteId IS NULL OR o.clienteId = :clienteId)
              AND (:veiculoId IS NULL OR o.veiculoId = :veiculoId)
              AND o.criadoEm >= COALESCE(:inicio, o.criadoEm)
              AND o.criadoEm <= COALESCE(:fim, o.criadoEm)
            """)
    Page<OrdemServico> buscar(@Param("status") StatusOrdemServico status,
                              @Param("clienteId") Long clienteId,
                              @Param("veiculoId") Long veiculoId,
                              @Param("inicio") LocalDateTime inicio,
                              @Param("fim") LocalDateTime fim,
                              Pageable pageable);
}
