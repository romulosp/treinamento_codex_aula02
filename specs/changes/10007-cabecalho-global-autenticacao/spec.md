# SPEC: 10007-cabecalho-global-autenticacao

## Status

`SPEC_APROVADA`

## Referencias e dependencias

- [Proposta](proposal.md).
- [Design](DESIGN.md).
- [Material de origem](source-material/README.md).
- `autenticadorsso`, `:plugin-login`, `:shared-api` e `:app`.
- Changes arquivadas 10001 e 10003; Change 10006 para aparencia do login.

## Requisitos funcionais

### RF-01 - Variante deslogada

Sem sessao valida, o host DEVE mostrar cabecalho branco de aproximadamente
64 dp, divisor inferior, `Buy More` a esquerda, `POS - COMPRAS` centralizado e
`v1.0.0.0` a direita, conforme `template_base_sem_autenticar.svg`.

### RF-02 - Perfil autenticado fornecido pelo backend

Ao receber sucesso do Keycloak, `autenticadorsso` DEVE processar o access token
no backend e produzir um perfil minimo com:

- `username`, proveniente de `preferred_username`;
- `displayName`, proveniente de `name`, com fallback na concatenacao nao vazia
  de `given_name` e `family_name`;
- `roleLabel`, derivado exclusivamente das roles reconhecidas em
  `realm_access.roles`.

A resposta de autenticacao DEVE continuar contendo `sessaoId` opaco e
`expiraEmEpochMillis`, acrescentando o perfil sanitizado. Access token, refresh
token, `jti`, `sid`, `sub`, email e demais claims NAO DEVEM sair do backend.

O backend DEVE decodificar somente o payload do access token devolvido pela
mesma chamada autenticada ao endpoint de token do Keycloak. Esta Change NAO
DEVE adicionar JWKS, introspeccao nem validacao criptografica local. A decisao
aceita explicitamente o risco residual de confiar nessa resposta do provedor.
Token ausente, JWT malformado, payload invalido, username, nome exibivel ou role
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

### RF-03 - Mapeamento e precedencia de roles

O mapeamento visual DEVE ser:

| Role do realm | Label |
| --- | --- |
| `OPERADOR_ROLE` | `OPERADOR` |
| `SUPERVISOR_ROLE` | `SUPERVISOR` |
| `PROPRIETARIO_ROLE` | `PROPRIETARIO` |

Quando houver mais de uma role reconhecida, a precedencia DEVE ser
`PROPRIETARIO_ROLE`, depois `SUPERVISOR_ROLE`, depois `OPERADOR_ROLE`. Roles
como `ADM_ROLE`, `SERVICE_ROLE` e `USER_ROLE` nao autorizam um label por
aproximacao. Ausencia de role reconhecida impede a criacao da sessao visual.

### RF-04 - Variante logada

Com sessao valida, o host DEVE mostrar cabecalho branco de aproximadamente
120 dp com marca, titulo e versao, acrescentando o quadro informativo da
referencia autenticada. A primeira linha DEVE seguir a forma:

`<ROLE_LABEL>: <USERNAME> - <DISPLAY_NAME>`

Na interface, o label `PROPRIETARIO` DEVE ser apresentado como
`PROPRIETÁRIO`. A normalizacao visual nao altera o identificador da role.

A linha seguinte DEVE mostrar somente o label `TERMINAL`, sem valor, separador
ou fallback. `LOTÉRICA` NAO DEVE ser exibida. Nenhum desses dados existe nos
claims aprovados e esta Change nao cria configuracao nem inventa valor ausente.

### RF-05 - Persistencia e transicao

O cabecalho DEVE pertencer ao shell do `:app` e permanecer acima do plugin de
login, do menu e de toda tela de plugin de negocio. A mudanca de sessao ausente
para presente troca atomicamente a variante; logout, expiracao ou limpeza da
sessao restaura a variante deslogada e remove o perfil da memoria do host.

Plugins nao podem ocultar, substituir ou sobrepor o cabecalho. O
`:plugin-login` NAO DEVE manter uma segunda instancia depois da ativacao do
shell global.

### RF-06 - Contrato Android

O resultado de autenticacao no plugin e `PluginEvent.SessionStateChanged`
DEVEM transportar somente `sessaoId`, expiracao e um modelo imutavel do perfil
sanitizado. A versao minor de `SharedApi` DEVE ser incrementada e os manifestos
compativeis atualizados de forma coordenada.

O componente Compose DEVE receber um tipo fechado `LoggedOut` ou
`LoggedIn(profile)` e `Modifier` aplicado na raiz. Ele nao
possui estado proprio nem executa autenticacao, parsing de JWT ou efeitos.

## Requisitos nao funcionais

- JWT e refresh token permanecem somente no backend e nunca aparecem em logs,
  excecoes, rotas, memoria de UI, testes ou screenshots.
- Respostas REST nao devem incluir email ou claims sem uso no cabecalho.
- Textos longos devem ter truncamento deterministico e descricao semantica
  completa; font scale 1,5 nao pode sobrepor conteudo interativo.
- Layout adaptativo em paisagem, sem coordenadas absolutas dependentes de
  1280x800 e sem empacotar os SVGs.
- Java alterado deve manter JavaDoc em portugues do Brasil; Kotlin publico ou
  protegido e contratos internos nao obvios devem manter KDoc.
- O export do realm, suas senhas e client secrets nao podem ser copiados para a
  Change. A implementacao deve usar configuracao de ambiente ja prevista.

## Regras de negocio

- Sessao ausente implica `LoggedOut`; sessao valida com perfil implica
  `LoggedIn`.
- Labels sao apresentacao de perfil, nao mecanismo de autorizacao.
- O backend e a unica fronteira autorizada a decodificar e extrair claims do
  JWT; a validacao criptografica adicional permanece fora do escopo aceito.
- Valor de terminal e loterica nao pode ser derivado de username, nome ou role.

## Cenarios e criterios de aceite

1. Sem sessao, o cabecalho deslogado aparece uma unica vez acima do login.
2. JWT decodificavel com `OPERADOR_ROLE` produz `OPERADOR`; os outros dois mapeamentos
   e a precedencia de multiplas roles sao comprovados por testes unitarios.
3. Sucesso REST devolve `autenticado=true`, sessao, expiracao e exatamente os
   tres campos de perfil; testes comprovam ausencia de tokens e claims nao
   permitidos.
4. JWT malformado, perfil incompleto ou sem role reconhecida nao abre o menu.
5. Menu, tela de plugin e retorno preservam o mesmo cabecalho logado.
6. Logout e expiracao limpam perfil e restauram a variante deslogada.
7. Em 1280x800, as duas variantes sao comparadas aos SVGs; viewport paisagem
   menor e font scale 1,5 nao apresentam sobreposicao.
8. Maven do backend, Gradle dos modulos Android, testes unitarios/integracao/UI,
   lint e validadores terminam com codigo zero ou a limitacao e registrada.
9. Testes do backend cobrem de/para e precedencia de roles, token ausente,
   formato JWT malformado e payload invalido, sem reproduzir tokens reais.
10. A variante logada mostra `TERMINAL` sem valor e nao mostra `LOTÉRICA`.
