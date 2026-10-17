package br.com.oficinatrack.relatorios.api;

import br.com.oficinatrack.relatorios.application.RelatorioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {
    private final RelatorioService service;

    public RelatorioController(RelatorioService service) { this.service = service; }

    @GetMapping("/tempo-medio-execucao")
    @PreAuthorize("hasRole('GESTOR')")
    public TempoMedioExecucaoResponse tempoMedioExecucao() {
        return service.tempoMedioExecucao();
    }
}
