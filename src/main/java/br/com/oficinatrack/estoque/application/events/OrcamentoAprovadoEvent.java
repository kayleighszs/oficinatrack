package br.com.oficinatrack.estoque.application.events;

import java.util.List;

/**
 * MOLDE PROVISÓRIO — aguardando definição final do módulo de OS.
 * Este record existe apenas para permitir o desenvolvimento e teste
 * isolado do EstoqueEventListener. Quando o módulo de OS publicar
 * o evento real, este contrato deve ser substituído/realinhado.
 */
public record OrcamentoAprovadoEvent(Long ordemServicoId, List<ItemPecaAprovado> itens) {
    public record ItemPecaAprovado(Long pecaId, Integer quantidade) {}
}