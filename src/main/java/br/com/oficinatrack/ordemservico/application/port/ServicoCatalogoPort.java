package br.com.oficinatrack.ordemservico.application.port;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Porta de saída da OS para o catálogo de serviços. Define só o que a OS precisa,
 * sem expor DTOs ou classes do módulo de catálogo.
 */
public interface ServicoCatalogoPort {

    /** @return o serviço do catálogo, ou vazio quando não existe */
    Optional<ServicoCatalogo> buscar(Long servicoId);

    record ServicoCatalogo(Long id, String nome, BigDecimal valor, boolean ativo) {
    }
}
