package br.com.oficinatrack.veiculo.infra;

import br.com.oficinatrack.veiculo.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    boolean existsByPlacaValue(String placa);

    @Query("""
            select v from Veiculo v
            where (:clienteId is null or v.cliente.id = :clienteId)
              and (:ativo is null or v.ativo = :ativo)
            order by v.id
            """)
    List<Veiculo> buscar(@Param("clienteId") Long clienteId, @Param("ativo") Boolean ativo);
}
