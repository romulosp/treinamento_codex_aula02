# Modelo de ameaças

## Escopo de segurança

O modelo cobre APKs de plugins internos carregados pelo mesmo processo do host.
Ele não afirma isolamento de código: um plugin aceito deve ser considerado tão
confiável quanto uma dependência compilada no app.

## Ativos e limites

| Ativo | Limite de confiança |
| --- | --- |
| APK em staging | não confiável até verificação completa |
| APK promovido e digest | armazenamento privado do host |
| Allowlist de certificados | build/release do host, imutável em runtime |
| API compartilhada | host como classloader pai |
| Sessão e segredos | coordenador de sessão; nunca roteador de plugins |
| UI ativa | host controla contêiner e recuperação de falha |

## Ameaças e controles

| Ameaça | Controle obrigatório | Evidência futura |
| --- | --- | --- |
| APK substituído após descoberta | cópia para quarentena privada antes da análise/carga | teste de troca de arquivo |
| APK assinado por emissor não permitido | pin SHA-256 de certificados e verificação anterior ao classloader | teste de certificado inválido |
| Manifesto adulterado | schema, identidade, pacote, API e entry class conferidos | testes parametrizados |
| Downgrade/incompatibilidade de API | major/minor versionados e política de rejeição | teste de matriz ABI |
| Token/senha em evento ou log | tipos de eventos sem segredo, redaction e testes de logs | busca automatizada e teste |
| Plugin falha durante callback | contenção de exceção, estado `ERROR`, fallback do host | teste instrumentado |
| Atualização corrompe UI ativa | `PENDING_RESTART`; nenhum hot swap | teste de atualização |
| Plugin malicioso obtém permissões do host | fora de escopo: somente código interno confiável; futura solução por processo/IPC separado | ADR e revisão futura |

## Premissas

- A chave de assinatura permitida é protegida fora do dispositivo.
- O processo que deposita o APK em staging é identificado antes da produção.
- Integridade e autorização de backend não fazem parte desta Change.
