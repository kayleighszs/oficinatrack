package br.com.oficinatrack.publico.infra;

import br.com.oficinatrack.publico.domain.AcessoPublicoOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface AcessoPublicoRepository extends JpaRepository<AcessoPublicoOrdemServico, Long> {
    Optional<AcessoPublicoOrdemServico> findByTokenHash(String tokenHash);
    List<AcessoPublicoOrdemServico> findAllByOrdemServicoIdAndRevogadoEmIsNull(Long ordemServicoId);
    Optional<AcessoPublicoOrdemServico> findByIdAndOrdemServicoId(Long id, Long ordemServicoId);
}
