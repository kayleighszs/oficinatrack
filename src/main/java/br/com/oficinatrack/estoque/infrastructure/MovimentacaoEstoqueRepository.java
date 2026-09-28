package br.com.oficinatrack.estoque.infrastructure;

import br.com.oficinatrack.estoque.domain.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long>{

}
