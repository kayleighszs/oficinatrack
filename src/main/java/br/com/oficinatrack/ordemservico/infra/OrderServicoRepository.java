package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.ordemservico.domain.OrdemServico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderServicoRepository extends JpaRepository<OrdemServico, Long> {
    Page<OrdemServico> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
