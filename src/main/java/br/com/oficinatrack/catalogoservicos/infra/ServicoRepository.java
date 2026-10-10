package br.com.oficinatrack.catalogoservicos.infra;

import br.com.oficinatrack.catalogoservicos.domain.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    Page<Servico> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
