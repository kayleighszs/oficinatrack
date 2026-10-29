package br.com.oficinatrack.ordemservico.application.port;

public interface CatalogoServicoPort {

    ItemOrcavel buscarServicoAtivo(Long servicoId);
}
