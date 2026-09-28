package br.com.oficinatrack.ordemservico.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusOrdemServicoTest {

    @ParameterizedTest
    @CsvSource({
            "RECEBIDA, EM_DIAGNOSTICO",
            "RECEBIDA, CANCELADA",
            "EM_DIAGNOSTICO, AGUARDANDO_APROVACAO",
            "EM_DIAGNOSTICO, CANCELADA",
            "AGUARDANDO_APROVACAO, EM_EXECUCAO",
            "AGUARDANDO_APROVACAO, ORCAMENTO_RECUSADO",
            "AGUARDANDO_APROVACAO, CANCELADA",
            "ORCAMENTO_RECUSADO, EM_DIAGNOSTICO",
            "ORCAMENTO_RECUSADO, CANCELADA",
            "EM_EXECUCAO, FINALIZADA",
            "FINALIZADA, ENTREGUE"
    })
    void permiteTransicoesValidas(StatusOrdemServico origem, StatusOrdemServico destino) {
        assertTrue(origem.podeTransicionarPara(destino));
    }

    @ParameterizedTest
    @CsvSource({
            "RECEBIDA, EM_EXECUCAO",
            "RECEBIDA, FINALIZADA",
            "EM_DIAGNOSTICO, EM_EXECUCAO",
            "AGUARDANDO_APROVACAO, FINALIZADA",
            "EM_EXECUCAO, CANCELADA",
            "EM_EXECUCAO, ENTREGUE",
            "FINALIZADA, CANCELADA",
            "FINALIZADA, EM_EXECUCAO"
    })
    void bloqueiaTransicoesInvalidas(StatusOrdemServico origem, StatusOrdemServico destino) {
        assertFalse(origem.podeTransicionarPara(destino));
    }

    @ParameterizedTest
    @EnumSource(StatusOrdemServico.class)
    void naoPermiteTransicaoParaSiMesmoNemNula(StatusOrdemServico status) {
        assertFalse(status.podeTransicionarPara(status));
        assertFalse(status.podeTransicionarPara(null));
    }

    @ParameterizedTest
    @EnumSource(value = StatusOrdemServico.class, names = {"ENTREGUE", "CANCELADA"})
    void estadosTerminaisNaoTemSaida(StatusOrdemServico status) {
        assertTrue(status.isTerminal());
        for (StatusOrdemServico destino : StatusOrdemServico.values()) {
            assertFalse(status.podeTransicionarPara(destino));
        }
    }

    @Test
    void estadosIntermediariosNaoSaoTerminais() {
        assertFalse(StatusOrdemServico.RECEBIDA.isTerminal());
        assertFalse(StatusOrdemServico.EM_EXECUCAO.isTerminal());
    }
}
