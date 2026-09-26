# Auditoria de segurança: 10003-sso-menu-negocio

## Resultado

`SEM_ACHADOS_CONFIRMADOS`

## Escopo

- API Quarkus de autenticação e logout.
- Integração Android, contrato compartilhado e tela inicial.
- Gerador/template do launcher e configuração de rede debug.
- Fontes e documentos da Change; artefatos de build foram excluídos.

## Evidências

| ID | Controle | Evidência | Resultado |
| --- | --- | --- | --- |
| `SEC-001` | Segredo fora do código e documentação | comparação em memória com a chave externa, sem imprimir o valor | Conforme |
| `SEC-002` | Tokens confinados ao backend | respostas contêm somente sessão opaca e expiração; tokens ficam no store em memória | Conforme |
| `SEC-003` | Identificador de sessão imprevisível | 32 bytes de `SecureRandom`, codificados em Base64 URL-safe | Conforme |
| `SEC-004` | Logs sanitizados | nenhuma credencial/token/resposta integral é registrada; falhas usam classe/código genérico | Conforme |
| `SEC-005` | Rede fora da main thread | autenticação usa `Dispatchers.IO`; logout usa executor serial do host | Conforme |
| `SEC-006` | Cleartext restrito ao desenvolvimento | configuração existe apenas em `src/debug` e autoriza somente `10.0.2.2` | Conforme |
| `SEC-007` | Entrada e respostas previsíveis | Bean Validation, DTOs e mapeadores para 400/401/503 | Conforme |
| `SEC-008` | Logout defensivo | sessão local é removida antes da tentativa remota e não é restaurada em falha | Conforme |
| `SEC-009` | Bootstrap debug não contorna confiança | APK incorporado entra no inbox e atravessa assinatura, manifesto, digest, quarentena e promoção normais | Conforme |
| `SEC-010` | Separação release | inspeção de `app-release-unsigned.apk` confirmou ausência do asset de bootstrap | Conforme |

## Risco aceito

O Direct Access Grant faz a senha atravessar aplicativo e backend e não é a
opção recomendada para novos clientes. A exceção está delimitada ao protótipo
intranet, foi aprovada na SPEC e registrada em
`ADR-001-direct-access-grant.md`. Qualquer uso fora desse contexto exige nova
decisão arquitetural, HTTPS e migração para Authorization Code com PKCE.

## Limitações

- A abertura do login foi capturada no emulador, mas a auditoria ainda não
  capturou tráfego SSO real com credencial válida.
- O store de sessão é volátil por decisão da SPEC; reinício invalida sessões.
- O BAT local contém as variáveis necessárias ao processo e é deliberadamente
  ignorado pelo Git; deve permanecer protegido pelas permissões do usuário.

## Conclusão

Não há achado confirmado no estado atual. A aprovação formal permanece
condicionada ao roteiro manual de `validation.md`.
