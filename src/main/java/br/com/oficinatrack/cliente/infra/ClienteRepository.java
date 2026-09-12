package br.com.oficinatrack.cliente.infra;

import br.com.oficinatrack.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
