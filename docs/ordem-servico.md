# Ordem de Serviço — passo a passo

Base: `http://localhost:8080`

Todos os exemplos usam `curl`. Se preferir, o mesmo fluxo funciona pelo Swagger em `http://localhost:8080/swagger-ui.html`.

## Como a OS funciona (resumo)

1. A OS nasce com status **RECEBIDA**, ligada a um cliente e a um veículo, e recebe um número no formato `OS-XXXXXXXX-XXX`.
2. O **diagnóstico** leva a OS para **EM_DIAGNOSTICO**. Só nesse status dá para adicionar **serviços** e **peças**.
3. Cada item **copia** o nome e o valor do catálogo/estoque na hora da inclusão. Se o preço mudar depois, a OS não muda. Um mesmo serviço ou peça não pode entrar duas vezes na mesma OS.
4. O **orçamento** é calculado pelo backend: `soma(serviços) + soma(peças)`. O total nunca vem do client. Se você mudar os itens depois de calcular, o orçamento antigo é substituído e deve ser recalculado.
5. **Enviar** o orçamento leva a OS para **AGUARDANDO_APROVACAO** e trava a inclusão de itens.
6. O cliente **aprova ou recusa** pelo acesso público, sem login. Aprovar baixa o estoque das peças e leva a OS para **EM_EXECUCAO**. Recusar leva para **ORCAMENTO_RECUSADO**.
7. A OS é **finalizada** (`FINALIZADA`) e depois **entregue** (`ENTREGUE`).

```
RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → EM_EXECUCAO → FINALIZADA → ENTREGUE
                                      └──────────→ ORCAMENTO_RECUSADO
```

Cada mudança de status fica registrada no histórico da OS.

## Quem pode fazer o quê

| Ação | Rota | Perfis |
|---|---|---|
| Criar OS | `POST /ordens-servico` | GESTOR, ATENDENTE |
| Iniciar diagnóstico | `POST /ordens-servico/{id}/diagnosticos` | GESTOR, MECANICO |
| Adicionar serviço | `POST /ordens-servico/{id}/itens-servico` | GESTOR, MECANICO |
| Adicionar peça | `POST /ordens-servico/{id}/itens-peca` | GESTOR, MECANICO |
| Calcular orçamento | `POST /ordens-servico/{id}/orcamentos` | GESTOR, ATENDENTE |
| Enviar orçamento | `POST /ordens-servico/{id}/orcamentos/{orcamentoId}/envios` | GESTOR, ATENDENTE |
| Emitir acesso público | `POST /ordens-servico/{id}/acessos-publicos` | GESTOR, ATENDENTE |
| Finalizar | `POST /ordens-servico/{id}/finalizacoes` | GESTOR, MECANICO |
| Entregar | `POST /ordens-servico/{id}/entregas` | GESTOR, ATENDENTE |
| Consultar OS, listar, histórico | `GET /ordens-servico`, `/{id}`, `/{id}/historico-status` | todos |

Quem não tem o perfil recebe **403**. Sem token também recebe **403**.

## Antes de testar

### 1. Suba o projeto

```bash
cp .env.example .env
docker compose up --build
```

### 2. Faça login e guarde o token

Todas as rotas, exceto as de `/publico`, exigem token. O gestor tem acesso a tudo, então é o mais simples para este roteiro:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" \
  -d '{"usuario":"gestor","senha":"troque-esta-senha"}' | sed -E 's/.*"token":"([^"]+)".*/\1/')
```

(Use o usuário e a senha que você definiu no `.env`. O token vale 30 minutos.)

> **Windows:** no Git Bash e no PowerShell, evite acentos nos exemplos (`Joao`, não `João`). Eles podem ser enviados em outra codificação e a API responde "corpo malformado".

## cURLs (fluxo completo)

Os comandos abaixo usam os ids `1` como exemplo. Se o seu banco já tiver dados, troque pelos ids que a API devolver.

```bash
H_JSON="Content-Type: application/json"
H_AUTH="Authorization: Bearer $TOKEN"

# 1. Cliente (retorna 201)
curl -i -X POST http://localhost:8080/clientes -H "$H_JSON" -H "$H_AUTH" \
  -d '{"nome":"Joao da Silva","cpfCnpj":"529.982.247-25","telefone":"(11) 91234-5678","email":"joao@email.com"}'

# 2. Pegue o id do cliente (campo "id" na resposta)
curl "http://localhost:8080/clientes?cpfCnpj=52998224725" -H "$H_AUTH"

# 3. Veiculo do cliente (troque clienteId pelo id do passo 2)
curl -X POST http://localhost:8080/veiculos -H "$H_JSON" -H "$H_AUTH" \
  -d '{"clienteId":1,"placa":"ABC1D23","marca":"Fiat","modelo":"Uno","ano":2015}'

# 4. Servico do catalogo (guarde o "id" da resposta)
curl -X POST http://localhost:8080/servicos -H "$H_JSON" -H "$H_AUTH" \
  -d '{"nome":"Troca de oleo","valor":80.00,"tempoMedioEstimado":30}'

# 5. Peca no estoque (guarde o "id"; so o GESTOR cadastra pecas)
curl -X POST http://localhost:8080/pecas -H "$H_JSON" -H "$H_AUTH" \
  -d '{"nome":"Filtro de oleo","valorUnitario":35.25,"quantidadeEstoque":10,"estoqueMinimo":2}'

# 6. Criar a OS (troque clienteId e veiculoId). Guarde o "id" e o "numeroOs"
curl -X POST http://localhost:8080/ordens-servico -H "$H_JSON" -H "$H_AUTH" \
  -d '{"clienteId":1,"veiculoId":1}'

# 7. Iniciar o diagnostico (OS 1)
curl -X POST http://localhost:8080/ordens-servico/1/diagnosticos -H "$H_AUTH"

# 8. Adicionar servico (referenciaId e o id do servico)
curl -X POST http://localhost:8080/ordens-servico/1/itens-servico -H "$H_JSON" -H "$H_AUTH" \
  -d '{"referenciaId":1,"quantidade":2}'

# 9. Adicionar peca (referenciaId e o id da peca)
curl -X POST http://localhost:8080/ordens-servico/1/itens-peca -H "$H_JSON" -H "$H_AUTH" \
  -d '{"referenciaId":1,"quantidade":3}'

# 10. Calcular o orcamento (guarde o id em "orcamentos[0].id"; total esperado: 265.75)
curl -X POST http://localhost:8080/ordens-servico/1/orcamentos -H "$H_AUTH"

# 11. Enviar o orcamento (OS 1, orcamento 1) -> AGUARDANDO_APROVACAO
curl -X POST http://localhost:8080/ordens-servico/1/orcamentos/1/envios -H "$H_AUTH"

# 12. Emitir o acesso publico do cliente. O "token" aparece UMA unica vez
curl -X POST http://localhost:8080/ordens-servico/1/acessos-publicos -H "$H_AUTH"
```

### Visão do cliente (sem login)

Use o `token` e o `numeroOs` do passo 12. Aqui o `Authorization` é o **token público**, não o JWT:

```bash
NUMERO_OS="OS-XXXXXXXX-XXX"     # numeroOs devolvido no passo 12
TOKEN_PUBLICO="cole-o-token-do-passo-12"

# 13. Ver o status da OS
curl http://localhost:8080/publico/ordens-servico/$NUMERO_OS/status \
  -H "Authorization: Bearer $TOKEN_PUBLICO"

# 14. Ver o orcamento
curl http://localhost:8080/publico/ordens-servico/$NUMERO_OS/orcamentos/1 \
  -H "Authorization: Bearer $TOKEN_PUBLICO"

# 15. Aprovar (baixa o estoque e a OS vai para EM_EXECUCAO)
curl -X POST http://localhost:8080/publico/ordens-servico/$NUMERO_OS/orcamentos/1/decisoes \
  -H "Content-Type: application/json" -H "Authorization: Bearer $TOKEN_PUBLICO" \
  -d '{"decisao":"APROVADO"}'

# Ou recusar (a OS vai para ORCAMENTO_RECUSADO)
#   -d '{"decisao":"RECUSADO","motivo":"Valor acima do esperado"}'
```

### Fechando a OS

```bash
# 16. Finalizar (EM_EXECUCAO -> FINALIZADA)
curl -X POST http://localhost:8080/ordens-servico/1/finalizacoes -H "$H_AUTH"

# 17. Confirmar a entrega (FINALIZADA -> ENTREGUE)
curl -X POST http://localhost:8080/ordens-servico/1/entregas -H "$H_AUTH"

# Consultar o historico de status
curl http://localhost:8080/ordens-servico/1/historico-status -H "$H_AUTH"
```

Resultado esperado no histórico: `RECEBIDA → EM_DIAGNOSTICO → AGUARDANDO_APROVACAO → EM_EXECUCAO → FINALIZADA → ENTREGUE`.
Depois da aprovação, o estoque da peça cai de 10 para 7 (`GET /pecas/1`).

## Erros que você pode conferir

| Situação | Resposta |
|---|---|
| Cliente ou veículo inexistente/inativo, ou veículo de outro cliente, ao criar a OS | **400** |
| Corpo vazio ou campos obrigatórios ausentes (lista os campos com problema) | **400** |
| OS inexistente | **404** |
| Sem token, token expirado ou perfil sem permissão | **403** |
| Token público inválido ou expirado | **401** |
| Adicionar item fora do status `EM_DIAGNOSTICO` (por exemplo, depois de enviar o orçamento) | **409** |
| Adicionar o mesmo serviço ou peça duas vezes | **409** |
| Calcular o orçamento sem nenhum item | **409** |
| Enviar um orçamento que não é o mais recente | **409** |
| Aprovar com estoque insuficiente, ou com peça inativa | **409** |

```bash
# Corpo vazio (400, com a lista de campos)
curl -i -X POST http://localhost:8080/ordens-servico -H "$H_JSON" -H "$H_AUTH" -d '{}'

# OS inexistente (404)
curl -i http://localhost:8080/ordens-servico/9999 -H "$H_AUTH"

# Sem token (403)
curl -i http://localhost:8080/ordens-servico/1

# Calcular orcamento sem itens (409): crie uma OS nova, inicie o diagnostico e chame direto
curl -i -X POST http://localhost:8080/ordens-servico/2/orcamentos -H "$H_AUTH"
```

> O estoque só é conferido e baixado **na aprovação** do orçamento. Ao adicionar a peça na OS, a quantidade não é validada contra o estoque.
