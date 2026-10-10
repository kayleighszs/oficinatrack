package br.com.oficinatrack.ordemservico.infra;

import br.com.oficinatrack.cliente.application.ClienteService;
import br.com.oficinatrack.ordemservico.application.port.ClienteConsultaPort;
import br.com.oficinatrack.shared.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Component;

@Component
public class ClienteConsultaAdapter implements ClienteConsultaPort {

    private final ClienteService clienteService;

    public ClienteConsultaAdapter(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Override
    public boolean existeAtivo(Long clienteId) {
        try {
            return Boolean.TRUE.equals(clienteService.buscarPorId(clienteId).getAtivo());
        } catch (RecursoNaoEncontradoException ex) {
            return false;
        }
    }
}
