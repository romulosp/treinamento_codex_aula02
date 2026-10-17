# Revisão da SPEC — 10007-cabecalho-global-autenticacao

## Status

`REPROVADA`

## Escopo revisado

Foram revisados `proposal.md`, `spec.md`, `DESIGN.md`, `tasks.md`,
`validation.md`, o workflow e as convenções compartilhadas, a arquitetura de
plugins Android, a arquitetura do backend Java, a estratégia de testes e os
contratos atuais de `autenticadorsso`, `:shared-api`, `:plugin-login` e `:app`.

Os SVGs e o export do realm foram tratados somente como fontes de requisitos.
Nenhum token, segredo, senha ou dado pessoal foi reproduzido nesta revisão.

## Achados

| ID | Severidade | Evidência | Impacto | Recomendação |
| --- | --- | --- | --- | --- |
| `REV-001` | Bloqueante | `spec.md`, RF-02 exige validar origem e integridade do access token, e `DESIGN.md` atribui essa tarefa a `KeycloakIdentityProvider`, mas não define o mecanismo nem as verificações obrigatórias. O adapter atual apenas consome o endpoint de token e não possui contrato para assinatura, emissor, destinatário/cliente, expiração ou algoritmo. | Implementações incompatíveis poderiam ser consideradas conformes; uma delas poderia apenas decodificar o payload, enquanto outra consultaria JWKS, introspecção ou endpoint autenticado. Isso impede comprovar o critério que rejeita token inválido e afeta diretamente a fronteira de segurança. | Escolher no contrato um mecanismo de validação compatível com o fluxo adotado e enumerar as verificações mínimas, a origem das configurações não secretas e o comportamento para falha de chave, issuer, cliente/audience, algoritmo e tempo. |
| `REV-002` | Importante | `spec.md`, RF-02 afirma que a resposta deve continuar contendo os campos atuais e acrescentar o perfil; o contrato atual também contém `autenticado`. Entretanto, o cenário 3 exige que o sucesso devolva “somente sessão, expiração e os três campos de perfil”. | Não é possível determinar se `autenticado` permanece, é removido ou passa a ser redundante. Backend e cliente Android podem evoluir para formatos diferentes, e o teste de minimização do DTO não tem uma asserção única. | Declarar o JSON de sucesso completo, inclusive a presença ou ausência de `autenticado`, e alinhar RF-02 e o cenário 3. |
| `REV-003` | Importante | `spec.md`, RF-04 e `DESIGN.md` determinam configuração não sensível do host para `TERMINAL` e `LOTÉRICA`, mas não definem nomes, fonte, escopo por build nem valores padrão. `tasks.md` também não prevê a criação desse contrato. | A implementação pode usar recursos, `BuildConfig`, propriedades locais ou valores literais distintos. Isso reduz a reprodutibilidade e torna impossível configurar e testar os campos sem conhecer uma decisão implícita do código. | Definir a fonte e os identificadores de configuração, a política por variante/ambiente e como ausência ou branco chegam ao fallback `NÃO INFORMADO`; incluir os cenários correspondentes nas tarefas e testes. |

## Verificações sem ressalva

- O ownership do cabeçalho no shell de `:app` está coerente com a topologia
  atual e evita dependência de plugins no host.
- O mapeamento e a precedência das três roles reconhecidas são verificáveis.
- O contrato proíbe propagar access token, refresh token e claims sem uso para
  o Android.
- A evolução minor da `SharedApi` e a atualização coordenada do plugin de
  login estão identificadas.
- Persistência visual, logout, expiração, adaptação de layout, semântica e
  font scale possuem critérios observáveis.
- Não foi identificada decisão que exija ADR além da correção do contrato da
  própria Change.

## Veredito

`REPROVADA`

O achado bloqueante e os achados importantes devem ser resolvidos nos
documentos da Change e submetidos a nova revisão formal. Enquanto isso,
`proposal.md` e `spec.md` permanecem em `EM_REVISAO_SPEC`, e a implementação
não está autorizada.
