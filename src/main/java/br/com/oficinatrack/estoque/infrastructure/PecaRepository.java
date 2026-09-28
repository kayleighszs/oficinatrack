package br.com.oficinatrack.estoque.infrastructure;

import br.com.oficinatrack.estoque.domain.Peca;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

public interface PecaRepository extends JpaRepository<Peca, Long> {

    @Query("""
        SELECT p FROM Peca p
        WHERE (:nome IS NULL OR LOWER(p.nome) LIKE LOWER(CONCAT('%', :nome, '%')))
        AND (
            :estoqueBaixo IS NULL
            OR (:estoqueBaixo = true AND p.quantidadeEstoque <= p.estoqueMinimo)
            OR (:estoqueBaixo = false AND p.quantidadeEstoque > p.estoqueMinimo)
        )
        """)
    Page<Peca> buscarComFiltros(@Param("nome") String nome,
                                @Param("estoqueBaixo") Boolean estoqueBaixo,
                                Pageable pageable);
}
