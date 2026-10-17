package br.com.oficinatrack.relatorios.application;

import br.com.oficinatrack.relatorios.api.TempoMedioExecucaoResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RelatorioService {
    private final JdbcTemplate jdbcTemplate;

    public RelatorioService(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public TempoMedioExecucaoResponse tempoMedioExecucao() {
        return jdbcTemplate.queryForObject("""
                SELECT COUNT(*) AS quantidade,
                       CAST(AVG(EXTRACT(EPOCH FROM (finalizada.alterado_em - execucao.alterado_em))) AS BIGINT) AS media
                FROM historico_status_ordem_servico execucao
                JOIN historico_status_ordem_servico finalizada
                  ON finalizada.ordem_servico_id = execucao.ordem_servico_id
                WHERE execucao.status_novo = 'EM_EXECUCAO'
                  AND finalizada.status_novo = 'FINALIZADA'
                """, (rs, rowNum) -> new TempoMedioExecucaoResponse(
                rs.getLong("quantidade"), rs.getObject("media", Long.class)));
    }
}
