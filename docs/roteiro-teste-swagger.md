# Roteiro de teste completo no Swagger

Este roteiro demonstra o fluxo funcional da Oficina Track desde a autenticação até a entrega do veículo.

Swagger: <http://localhost:8080/swagger-ui/index.html>

> Este roteiro usa dados que **já existem no banco**. Cliente, veículo, serviço e peça são apenas consultados, o que evita erros de CPF/placa duplicados. A OS é criada do zero durante a gravação.

## Dados de controle

| Valor | Dado pré-existente |
|---|---|
| `CLIENTE_ID` | `6` — Cliente Demonstração 1 (CPF `38641527044`) |
| `VEICULO_ID` | `5` — Honda Civic 2024, placa `DEM2A26` |
| `SERVICO_ID` | `5` — Alinhamento Demonstração, `149.90`, 60 min |
| `PECA_ID` | `5` — Pastilha de Freio Demonstração, `89.50`, estoque 30 |
| `ORDEM_ID` | Gerado no passo 6 (provavelmente `7`) |
| `NUMERO_OS` | Gerado no passo 6 |
| `ORCAMENTO_ID` | Gerado no passo 10 (provavelmente `6`) |
| `TOKEN_PUBLICO` | Gerado no passo 12 |

> Não use a OS `6` nem as OS `1` a `5`. A OS 6 recebeu itens errados e as demais já estão `ENTREGUE`, o que retorna `409` em qualquer alteração.

## 1. Autenticação administrativa

Execute `POST /auth/login`:

```json
{
  "usuario": "gestor",
  "senha": "troque-esta-senha"
}
```

Resultado esperado:

- HTTP `200`;
- resposta contendo `token` e `expiraEm`.

Copie o token. Clique em **Authorize** e cole-o no esquema de cima, `bearerAuth`, **sem** o prefixo `Bearer` (o Swagger já acrescenta). O esquema de baixo, `publicAccessToken`, só é usado no passo 13.

## 2. Consultar cliente

Execute `GET /clientes/{id}` com `id = 6`.

Resultado esperado:

- HTTP `200`;
- `nome: Cliente Demonstração 1`, `cpfCnpj: 38641527044`, `ativo: true`.

## 3. Consultar veículo

Execute `GET /veiculos/{id}` com `id = 5`.

Resultado esperado:

- HTTP `200`;
- `placa: DEM2A26`, `clienteId: 6`, `ativo: true`.

## 4. Consultar serviço

Execute `GET /servicos/{id}` com `id = 5`.

Resultado esperado: HTTP `200`, `nome: Alinhamento Demonstração`, `valor: 149.90`.

## 5. Consultar peça

Execute `GET /pecas/{id}` com `id = 5`.

Resultado esperado:

- HTTP `200`;
- `valorUnitario: 89.50`, `quantidadeEstoque: 30`;
- `estoqueBaixo: false`, `ativo: true`.

## 6. Criar ordem de serviço

Execute `POST /ordens-servico`:

```json
{
  "clienteId": 6,
  "veiculoId": 5
}
```

Resultado esperado:

- HTTP `201`;
- status `RECEBIDA`;
- primeiro histórico com `anterior: null` e `novo: RECEBIDA`.

Anote o `id` como `ORDEM_ID` e `numeroOs` como `NUMERO_OS`.

## 7. Iniciar diagnóstico

Execute `POST /ordens-servico/{id}/diagnosticos` com `id = ORDEM_ID`.

Resultado esperado: HTTP `200` e status `EM_DIAGNOSTICO`.

## 8. Adicionar serviço à OS

Execute `POST /ordens-servico/{id}/itens-servico` com `id = ORDEM_ID`:

```json
{
  "referenciaId": 5,
  "quantidade": 1
}
```

`referenciaId = 5` é o `SERVICO_ID`.

Resultado esperado: HTTP `200`, serviço presente em `servicos` e subtotal `149.90`.

## 9. Adicionar peça à OS

Execute `POST /ordens-servico/{id}/itens-peca` com `id = ORDEM_ID`:

```json
{
  "referenciaId": 5,
  "quantidade": 2
}
```

`referenciaId = 5` é o `PECA_ID`.

Resultado esperado: HTTP `200`, peça presente em `pecas` e subtotal `179.00`.

## 10. Calcular orçamento

Execute `POST /ordens-servico/{id}/orcamentos` com `id = ORDEM_ID`.

Resultado esperado:

- HTTP `200`;
- status do orçamento `CALCULADO`;
- total `328.90`, calculado por `149.90 + (89.50 × 2)`.

Anote o `id` do orçamento como `ORCAMENTO_ID`.

## 11. Enviar orçamento

Execute `POST /ordens-servico/{id}/orcamentos/{orcamentoId}/envios`:

- `id = ORDEM_ID`;
- `orcamentoId = ORCAMENTO_ID`.

Resultado esperado:

- HTTP `200`;
- status da OS `AGUARDANDO_APROVACAO`;
- status do orçamento `ENVIADO`;
- `enviadoEm` preenchido.

## 12. Gerar acesso público

Execute `POST /ordens-servico/{ordemId}/acessos-publicos` com `ordemId = ORDEM_ID`.

Resultado esperado:

- HTTP `201`;
- resposta contendo `token`, `numeroOs` e `expiraEm`.

Anote o token como `TOKEN_PUBLICO`.

## 13. Autorizar acesso público

Clique em **Authorize**, localize o esquema de baixo, `publicAccessToken`, e cole o `TOKEN_PUBLICO` sem o prefixo `Bearer`.

O token público e o token administrativo pertencem a esquemas diferentes.

## 14. Consultar status público

Execute `GET /publico/ordens-servico/{numeroOs}/status` com `numeroOs = NUMERO_OS`.

Resultado esperado:

- HTTP `200`;
- status `AGUARDANDO_APROVACAO`;
- `orcamentoAtualId = ORCAMENTO_ID`.

## 15. Consultar orçamento público

Execute `GET /publico/ordens-servico/{numeroOs}/orcamentos/{orcamentoId}`:

- `numeroOs = NUMERO_OS`;
- `orcamentoId = ORCAMENTO_ID`.

Resultado esperado: HTTP `200`, com serviços, peças e total do orçamento.

## 16. Aprovar orçamento como cliente

Execute `POST /publico/ordens-servico/{numeroOs}/orcamentos/{orcamentoId}/decisoes`:

```json
{
  "decisao": "APROVADO"
}
```

Resultado esperado:

- HTTP `200`;
- status da OS `EM_EXECUCAO`;
- status do orçamento `APROVADO`;
- `decididoEm` preenchido.

## 17. Finalizar serviço

Execute `POST /ordens-servico/{id}/finalizacoes` com `id = ORDEM_ID`.

Resultado esperado: HTTP `200` e status `FINALIZADA`.

## 18. Entregar veículo

Execute `POST /ordens-servico/{id}/entregas` com `id = ORDEM_ID`.

Resultado esperado: HTTP `200` e status final `ENTREGUE`.

## 19. Provar o histórico completo

Execute `GET /ordens-servico/{id}/historico-status` com `id = ORDEM_ID`.

Resultado esperado:

```text
RECEBIDA
→ EM_DIAGNOSTICO
→ AGUARDANDO_APROVACAO
→ EM_EXECUCAO
→ FINALIZADA
→ ENTREGUE
```

Confirme que todos os registros possuem `id` e `alteradoEm`, e que cada transição `anterior`/`novo` está correta.

## 20. Consulta final da OS

Execute `GET /ordens-servico/{id}` com `id = ORDEM_ID`.

Mostre como evidência final:

- cliente e veículo associados;
- status `ENTREGUE`;
- serviço e peças adicionados;
- orçamento `APROVADO`;
- total `328.90`;
- histórico completo.

## Checklist da gravação

- Mostrar a URL e o título do Swagger.
- Mostrar o endpoint, os parâmetros e o JSON de cada operação.
- Mostrar o código HTTP recebido.
- Anotar os IDs retornados e reutilizá-los corretamente.
- Mostrar cada mudança de status.
- Mostrar o cálculo do orçamento.
- Mostrar a troca para o token público.
- Encerrar mostrando a OS `ENTREGUE` e o histórico completo.
- Evitar deixar tokens visíveis por mais tempo que o necessário.

