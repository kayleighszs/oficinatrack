# Erros encontrados e resolvidos

Este documento registra os problemas encontrados durante a revisão e o teste completo da API pelo Swagger, incluindo causa, correção e validação.

## Resumo

| Problema | Impacto | Situação |
|---|---|---|
| IDs nulos no histórico | Respostas inconsistentes após mudança de status | Resolvido |
| Resposta criada antes do `flush` | Estado incompleto devolvido pela API | Resolvido |
| `atualizadoEm` não atualizado | Auditoria temporal incorreta | Resolvido |
| Histórico documentado como objeto | Contrato OpenAPI diferente da resposta real | Resolvido |
| Paginação incorreta no Swagger | Parâmetros difíceis ou impossíveis de testar | Resolvido |
| Token público misturado com JWT | Autorização confusa e incorreta | Resolvido |
| `Authorization` duplicado | Swagger mostrava campo manual desnecessário | Resolvido |
| Login documentado como protegido | Usuário precisava de token para obter token | Resolvido |
| Operações de criação documentadas como `200` | Contrato diferente do HTTP real | Resolvido |
| Schema de erro incorreto nos clientes | Resposta documentada diferente da aplicação | Resolvido |
| Schemas internos do Spring expostos | Documentação poluída | Resolvido |
| Placa duplicada retornava `400 Undocumented` | Erro real sem contrato OpenAPI | Resolvido |
| Autorização persistia no Swagger | Testes não começavam realmente do zero | Resolvido |
| Filtros opcionais de data no PostgreSQL | Consulta podia falhar com parâmetros nulos | Resolvido |

## 1. IDs nulos no histórico de status

### Sintoma

Após uma mudança de status, o registro recém-criado no histórico podia aparecer na resposta com `id: null`.

### Causa

A resposta era convertida para DTO antes de o Hibernate executar o `flush` e materializar os IDs gerados pelo banco.

### Correção

Foi centralizado o processo de persistir e mapear a OS:

```java
private OrdemServicoResponse persistirEMapear(OrdemServico os) {
    ordemRepository.flush();
    return OrdemServicoResponse.de(os);
}
```

As operações que alteram a ordem agora executam `flush` antes de gerar a resposta.

### Validação

O histórico final da OS testada apresentou IDs `25` a `30`, todos preenchidos.

## 2. Estado incompleto nas respostas da ordem de serviço

### Sintoma

Respostas de diagnóstico, itens, orçamento, aprovação, finalização e entrega podiam representar o estado anterior à persistência completa.

### Causa

Cada método montava `OrdemServicoResponse` diretamente, sem garantir a sincronização da sessão JPA com o banco.

### Correção

As operações passaram a usar `persistirEMapear` após a alteração da entidade.

### Validação

No teste completo, cada resposta apresentou imediatamente o novo status, os IDs e as datas esperadas.

## 3. Campo `atualizadoEm` desatualizado

### Sintoma

Alterações relevantes na OS não modificavam sempre o campo `atualizadoEm`.

### Correção

Foi criado o método interno:

```java
private void marcarAtualizada() {
    atualizadoEm = LocalDateTime.now();
}
```

Ele é acionado ao:

- adicionar serviço;
- adicionar peça;
- criar orçamento;
- alterar o status da ordem.

### Validação

As respostas do fluxo completo apresentaram `atualizadoEm` avançando após cada modificação.

## 4. Histórico com schema OpenAPI incorreto

### Sintoma

`GET /ordens-servico/{id}/historico-status` retornava uma lista, mas o Swagger não descrevia corretamente o resultado como array tipado.

### Correção

O retorno passou a ser explicitamente:

```java
List<OrdemServicoResponse.HistoricoResponse>
```

### Validação

O teste OpenAPI confirma que o schema da resposta `200` possui `type: array`.

## 5. Paginação incorreta no Swagger

### Sintoma

Os endpoints paginados mostravam um objeto `pageable` em vez dos parâmetros utilizáveis `page`, `size` e `sort`.

### Correção

Foi aplicado `@ParameterObject` aos parâmetros `Pageable`.

### Validação

O teste de integração confirma:

- existência de `page`, `size` e `sort`;
- ausência do parâmetro artificial `pageable`;
- execução real da paginação conforme documentada.

## 6. Token público misturado com JWT administrativo

### Sintoma

Os endpoints públicos apareciam com o mesmo esquema de autenticação usado pelos endpoints administrativos.

### Correção

Foi criado o esquema OpenAPI independente:

```text
publicAccessToken
```

Esse esquema representa o token Bearer opaco e temporário vinculado a uma única ordem de serviço.

### Validação

O orçamento foi consultado e aprovado utilizando somente o token público.

## 7. Cabeçalho `Authorization` duplicado

### Sintoma

Além do botão **Authorize**, os endpoints públicos exibiam um campo manual `Authorization`.

### Correção

Os parâmetros internos de cabeçalho foram ocultados da documentação com `@Parameter(hidden = true)`.

### Validação

O teste OpenAPI confirma que o parâmetro manual não está presente.

## 8. Login documentado como endpoint protegido

### Sintoma

`POST /auth/login` aparecia com requisito de autenticação, embora seja o endpoint responsável por emitir o JWT.

### Correção

O requisito de segurança foi removido da operação de login com `@SecurityRequirements`.

### Validação

O teste OpenAPI confirma que a lista `security` do login está vazia.

## 9. Códigos HTTP de criação incorretos

### Sintoma

Algumas operações retornavam `201 Created`, mas o Swagger apresentava `200` ou não registrava o código correto.

### Correção

As respostas `201` foram declaradas para:

- `POST /veiculos`;
- `POST /pecas`;
- `POST /ordens-servico`;
- `POST /ordens-servico/{ordemId}/acessos-publicos`.

### Validação

O teste manual confirmou `201` nas quatro operações.

## 10. Schema de erro incorreto nos clientes

### Sintoma

A documentação dos clientes utilizava `org.springframework.web.ErrorResponse`, diferente do corpo devolvido pelo tratamento global da aplicação.

### Correção

O import foi substituído por:

```java
br.com.oficinatrack.shared.exception.ErroResponse
```

### Validação

As respostas de erro agora mostram os campos reais:

- `timestamp`;
- `status`;
- `erro`;
- `mensagem`;
- `campos`.

## 11. Schemas internos do Spring expostos

### Sintoma

O Swagger mostrava modelos que não pertenciam ao contrato público, como `HttpHeaders` e `ProblemDetail`.

### Correção

Foi criado um `OpenApiCustomizer` para remover schemas internos não utilizados.

### Validação

O teste OpenAPI verifica a ausência de `HttpHeaders` e `ProblemDetail`.

## 12. Placa duplicada retornava `400 Undocumented`

### Sintoma

Ao cadastrar uma placa já existente, a API retornava corretamente HTTP `400`, mas o Swagger classificava a resposta como não documentada.

### Correção

`POST /veiculos` passou a declarar:

- `201`: veículo cadastrado com sucesso;
- `400`: dados inválidos ou placa já cadastrada;
- corpo `400` usando `ErroResponse`.

### Validação

O contrato publicado passou a apresentar:

```text
400  Dados inválidos ou placa já cadastrada
     #/components/schemas/ErroResponse
```

O Swagger foi inspecionado visualmente e não exibe mais `Undocumented` nesse caso.

## 13. Autorização persistente no Swagger

### Sintoma

Tokens anteriores podiam permanecer após recarregar a página, prejudicando testes e gravações iniciados do zero.

### Correção

Foi configurado:

```yaml
springdoc:
  swagger-ui:
    persist-authorization: false
```

### Validação

O Swagger foi recarregado na página inicial sem as autorizações e respostas anteriores.

## 14. Filtros opcionais de data no PostgreSQL

### Sintoma

Parâmetros de data opcionais podiam causar problemas de tipagem ou comparação quando enviados como nulos.

### Correção

A consulta passou a utilizar `COALESCE`:

```sql
o.criadoEm >= COALESCE(:inicio, o.criadoEm)
o.criadoEm <= COALESCE(:fim, o.criadoEm)
```

## Teste funcional completo

Foi executado no Swagger o seguinte fluxo:

```text
login
→ cadastro do cliente
→ cadastro do veículo
→ cadastro do serviço
→ cadastro da peça
→ criação da OS
→ diagnóstico
→ inclusão do serviço
→ inclusão da peça
→ cálculo do orçamento
→ envio do orçamento
→ emissão do acesso público
→ aprovação pública
→ finalização
→ entrega
```

Resultado final:

- OS de teste: ID `5`;
- orçamento: `328,90`;
- orçamento `APROVADO`;
- OS `ENTREGUE`;
- histórico completo e sem IDs nulos.

Sequência validada:

```text
RECEBIDA
→ EM_DIAGNOSTICO
→ AGUARDANDO_APROVACAO
→ EM_EXECUCAO
→ FINALIZADA
→ ENTREGUE
```

## Verificações técnicas

Foram executadas com sucesso:

```text
git diff --check
./mvnw -q -DskipTests test-compile
docker compose up -d --build app
```

O contrato `/v3/api-docs` e a interface do Swagger também foram verificados após a reconstrução do container.

