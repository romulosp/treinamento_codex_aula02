# Design: 034-automatizar-sonar-local

## Contexto

O repositório contém dois módulos Quarkus Maven e dois front-ends Vite. O SonarQube está em `D:\desenvolvimento\sonar`, ligado a PostgreSQL externo em `host.docker.internal:5432`, mas a composição PostgreSQL atual não prepara o banco `sonar`. O daemon Docker não está acessível no ambiente desta Change, portanto a integração real deve permanecer rastreada como pendência de ambiente, sem ocultar a contingência exigida.

## Decisões

1. Versionar o orquestrador em `scripts/sonar/validar-codigo.ps1`; manter a instalação local do Sonar fora do repositório como destino parametrizável, com padrão em `D:\desenvolvimento\sonar`.
2. Usar Docker Compose, pois há `docker-compose.yml` nas duas instalações e não há configuração de Swarm.
3. Considerar a saúde do container `postgres` da composição PostgreSQL, não somente a abertura da porta 5432. Assim, o script pode preparar o banco com segurança via `psql` no container.
4. Gerar senha aleatória no primeiro uso para `.env` do Sonar e passá-la ao PostgreSQL por entrada padrão, evitando senha em argumentos e logs. `SONAR_TOKEN` continua externo e obrigatório apenas na ação de análise.
5. Usar o código `20` exclusivamente para indisponibilidade operacional que exige auditoria LLM. Falhas de build, scanner ou Quality Gate mantêm erro comum e não podem virar aprovação por fallback.
6. Executar um scanner por módulo, preservando chaves de projeto independentes e suporte para Java, HTML, CSS, JavaScript e TypeScript.
7. Atualizar a Definition of Done e os prompts para que a auditoria LLM seja um gate alternativo apenas enquanto a ferramenta estiver ausente ou indisponível, com registro completo em `validation.md`.

## Fluxo

```text
validar-codigo.ps1 Tudo
  -> Docker acessível?
       não -> marcador + exit 20 -> auditoria LLM obrigatória
       sim -> PostgreSQL saudável?
                não -> docker compose up -d (PostgreSQL) -> aguarda
                     falha -> marcador + exit 20 -> auditoria LLM obrigatória
                sim -> garante usuário/banco sonar -> docker compose up -d (Sonar)
                     Sonar UP?
                       não -> marcador + exit 20 -> auditoria LLM obrigatória
                       sim -> compila módulos e executa scanner por módulo
                                -> Quality Gate/build/scanner falhou -> erro de validação
                                -> todos aprovados -> sucesso
```

## Componentes e arquivos

| Artefato | Responsabilidade |
| --- | --- |
| `scripts/sonar/validar-codigo.ps1` | Orquestrar pré-requisitos, banco, Sonar, build e scanner. |
| `scripts/sonar/tests/validar-codigo.Tests.ps1` | Provar sintaxe e contrato estrutural sem depender de Docker. |
| `D:\desenvolvimento\sonar\docker-compose.yml` | Consumir credenciais JDBC do `.env` local. |
| `specs/sprint/` e `.github/prompts/` | Exigir e registrar fallback LLM quando a infraestrutura de qualidade estiver indisponível. |

## Alternativas descartadas

- Usar senha `sonar` literal: descartado por expor credencial e tornar a instalação insegura.
- Considerar porta 5432 aberta como banco saudável: descartado, pois não comprova que a composição esperada está funcional.
- Tratar qualquer erro do scanner como fallback LLM: descartado, pois esconderia falha de Quality Gate ou configuração da análise.
- Instalar Sonar, Maven ou Node automaticamente: descartado por alterar ambiente e ocultar pré-requisitos fora do escopo.
