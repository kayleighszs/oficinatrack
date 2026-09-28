package br.com.oficinatrack.cliente.infra;

import br.com.oficinatrack.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByCpfCnpj(String cpfCnpj);

    @Query("SELECT c FROM Cliente c " +
            "WHERE c.ativo = true " +
            "AND (:nome = '' OR LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) " +
            "AND (:cpfCnpj = '' OR c.cpfCnpj LIKE CONCAT('%', :cpfCnpj, '%')) " +
            "ORDER BY c.nome")
    List<Cliente> buscarClientesComFiltros(@Param("nome") String nome,
                                           @Param("cpfCnpj") String cpfCnpj);
}
