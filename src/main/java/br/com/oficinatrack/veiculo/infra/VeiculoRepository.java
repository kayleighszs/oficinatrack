package br.com.oficinatrack.veiculo.infra;

import br.com.oficinatrack.veiculo.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
}
