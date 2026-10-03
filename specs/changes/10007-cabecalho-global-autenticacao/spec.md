# SPEC: 10007-cabecalho-global-autenticacao

**Autor:** Rômulo Penha

## Status

`SPEC_APROVADA`

## Referências e dependências

- [Proposta](proposal.md).
- [Design](DESIGN.md).
- [Material de origem](source-material/README.md).
- `autenticadorsso`, `:plugin-login`, `:shared-api` e `:app`.
- Changes arquivadas 10001 e 10003; Change 10006 para a aparência do login,
  exceto seu RF-02 e os critérios exclusivos do cabeçalho, superados por esta
  Change.

## Requisitos funcionais

### RF-01 — Variante deslogada

Sem sessão válida, o host DEVE mostrar cabeçalho branco de aproximadamente
64 dp, divisor inferior, `Buy More` à esquerda, `POS - COMPRAS` centralizado e
`v1.0.0.0` à direita, conforme `template_base_sem_autenticar.svg`.

### RF-02 — Perfil autenticado fornecido pelo backend

Ao receber sucesso do Keycloak, `autenticadorsso` DEVE processar o access token
no backend e produzir um perfil mínimo com:

- `username`, proveniente de `preferred_username`;
- `displayName`, proveniente de `name`, com fallback na concatenação não vazia
  de `given_name` e `family_name`;
- `roleLabel`, derivado exclusivamente das roles reconhecidas em
  `realm_access.roles`.

A resposta de autenticação DEVE continuar contendo `sessaoId` opaco e
`expiraEmEpochMillis`, acrescentando o perfil sanitizado. Access token, refresh
token, `jti`, `sid`, `sub`, email e demais claims NÃO DEVEM sair do backend.

O backend DEVE decodificar somente o payload do access token devolvido pela
mesma chamada autenticada ao endpoint de token do Keycloak. Esta Change NÃO
DEVE adicionar JWKS, introspecção nem validação criptográfica local. A decisão
aceita explicitamente o risco residual de confiar nessa resposta do provedor.
Token ausente, JWT malformado, payload inválido, username, nome exibível ou role
reconhecida ausentes DEVEM produzir indisponibilidade sem registrar o token.

O JSON de sucesso DEVE manter exatamente a forma abaixo, com `autenticado`
sempre `true`:

```json
{
  "autenticado": true,
  "sessaoId": "<identificador-opaco>",
  "expiraEmEpochMillis": 0,
  "perfil": {
    "username": "<preferred_username>",
    "displayName": "<nome-exibivel>",
    "roleLabel": "<label-sem-acento>"
  }
}
```

### RF-03 — Mapeamento e precedência de roles

O mapeamento visual DEVE ser:

| Role do realm | Label |
| --- | --- |
| `OPERADOR_ROLE` | `OPERADOR` |
| `SUPERVISOR_ROLE` | `SUPERVISOR` |
| `PROPRIETARIO_ROLE` | `PROPRIETARIO` |

Quando houver mais de uma role reconhecida, a precedência DEVE ser
`PROPRIETARIO_ROLE`, depois `SUPERVISOR_ROLE`, depois `OPERADOR_ROLE`. Roles
como `ADM_ROLE`, `SERVICE_ROLE` e `USER_ROLE` não autorizam um label por
aproximação. Ausência de role reconhecida impede a criação da sessão visual.

### RF-04 — Variante logada

Com sessão válida, o host DEVE mostrar cabeçalho branco de aproximadamente
120 dp com marca, título e versão, acrescentando o quadro informativo da
referência autenticada. A primeira linha DEVE seguir a forma:

`<ROLE_LABEL>: <USERNAME> - <DISPLAY_NAME>`

Na interface, o label `PROPRIETARIO` DEVE ser apresentado como
`PROPRIETÁRIO`. A normalização visual não altera o identificador da role.

A linha seguinte DEVE mostrar somente o label `TERMINAL`, sem valor, separador
ou fallback. `LOTÉRICA` NÃO DEVE ser exibida. Nenhum desses dados existe nos
claims aprovados e esta Change não cria configuração nem inventa valor ausente.

### RF-05 — Persistência e transição

O cabeçalho DEVE pertencer ao shell do `:app` e permanecer acima do plugin de
login, do menu e de toda tela de plugin de negócio. A mudança de sessão ausente
para presente troca atomicamente a variante; logout, expiração ou limpeza da
sessão restaura a variante deslogada e remove o perfil da memória do host.

Plugins não podem ocultar, substituir ou sobrepor o cabeçalho. O
`:plugin-login` NÃO DEVE manter uma segunda instância depois da ativação do
shell global.

> **Relação com Change 10006:** A Change 10006 implementou o cabeçalho dentro
> do layout de `:plugin-login`. Esta Change 10007 é a sucessora que transfere
> o cabeçalho para o shell global do `:app`. O trecho de cabeçalho da Change
> 10006 é considerado **superado** por esta Change; os demais elementos de
> layout do `:plugin-login` (gradiente, campos, teclados, rodapé) permanecem
> válidos.

### RF-06 — Contrato Android

O resultado de autenticação no plugin e `PluginEvent.SessionStateChanged`
DEVEM transportar somente `sessaoId`, expiração e um modelo imutável do perfil
sanitizado. A versão minor de `SharedApi` DEVE ser incrementada para `1.2.0` e
os manifestos compatíveis atualizados de forma coordenada. O manifesto
`startup-auth` DEVE exigir major 1 e minor 2. Plugins `business-menu` que exigem
major 1 e minor 1 permanecem compatíveis, pois o host aceita
`requiredSharedApiMinor <= hostMinor` nessa capacidade e eles não usam o evento
de perfil introduzido na minor 2.

O componente Compose DEVE receber um tipo fechado `LoggedOut` ou
`LoggedIn(profile)` e `Modifier` aplicado na raiz. Ele não
possui estado próprio nem executa autenticação, parsing de JWT ou efeitos.

## Requisitos não funcionais

- JWT e refresh token permanecem somente no backend e nunca aparecem em logs,
  exceções, rotas, memória de UI, testes ou screenshots.
- Respostas REST não devem incluir email ou claims sem uso no cabeçalho.
- Textos longos devem ter truncamento determinístico e descrição semântica
  completa; font scale 1,5 não pode sobrepor conteúdo interativo.
- Layout adaptativo em paisagem, sem coordenadas absolutas dependentes de
  1280×800 e sem empacotar os SVGs.
- Java alterado deve manter JavaDoc em português do Brasil; Kotlin público ou
  protegido e contratos internos não óbvios devem manter KDoc.
- O export do realm, suas senhas e client secrets não podem ser copiados para a
  Change. A implementação deve usar configuração de ambiente já prevista.

## Regras de negócio

- Sessão ausente implica `LoggedOut`; sessão válida com perfil implica
  `LoggedIn`.
- Labels são apresentação de perfil, não mecanismo de autorização.
- O backend é a única fronteira autorizada a decodificar e extrair claims do
  JWT; a validação criptográfica adicional permanece fora do escopo aceito.
- Valor de terminal e lotérica não pode ser derivado de username, nome ou role.

## Cenários e critérios de aceite

1. Sem sessão, o cabeçalho deslogado aparece uma única vez acima do login.
2. JWT decodificável com `OPERADOR_ROLE` produz `OPERADOR`; os outros dois mapeamentos
   e a precedência de múltiplas roles são comprovados por testes unitários.
3. Sucesso REST devolve `autenticado=true`, sessão, expiração e exatamente os
   três campos de perfil; testes comprovam ausência de tokens e claims não
   permitidos.
4. JWT malformado, perfil incompleto ou sem role reconhecida não abre o menu.
5. Menu, tela de plugin e retorno preservam o mesmo cabeçalho logado.
6. Logout e expiração limpam perfil e restauram a variante deslogada.
7. Em 1280×800, as duas variantes são comparadas aos SVGs; viewport paisagem
   menor e font scale 1,5 não apresentam sobreposição.
8. Maven do backend, Gradle dos módulos Android, testes unitários/integração/UI,
   lint e validadores terminam com código zero ou a limitação é registrada.
9. Testes do backend cobrem de/para e precedência de roles, token ausente,
   formato JWT malformado e payload inválido, sem reproduzir tokens reais.
10. A variante logada mostra `TERMINAL` sem valor e não mostra `LOTÉRICA`.
