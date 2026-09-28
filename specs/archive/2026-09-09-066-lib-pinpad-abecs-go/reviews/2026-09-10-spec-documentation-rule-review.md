# Revisão da regra de documentação Go — Change 066

**Data:** 2026-09-10
**Escopo:** RF-017, CA-021 a CA-023, tarefas e plano de implementação relacionados à documentação do código Go.

## Regra adicionada

A Change agora exige o uso permanente de `.agents/skills/golang-documentation/SKILL.md` para todo código Go novo, convertido, alterado ou gerado.

A regra cobre:

- comentários de pacote;
- comentários Godoc para funções, métodos, tipos, interfaces, constantes e variáveis exportadas;
- documentação de fluxos internos complexos de protocolo, concorrência, segurança, redaction e cancelamento;
- documentação de builders, parsers, modelos, erros, adaptadores, fila, worker, tracer e fachadas;
- exemplos executáveis quando aplicáveis;
- documentação de código gerado por templates ou ferramentas;
- proibição de dados sensíveis em comentários, exemplos, fixtures e documentação;
- inspeção documental registrada em `validation.md` e na revisão da implementação.

## Diagnóstico atual

A auditoria inicial do módulo Go identificou lacunas relevantes:

- ausência de comentários de pacote consistentes;
- vários tipos, interfaces, constantes, construtores e métodos exportados sem Godoc;
- comentários inline que não estão posicionados como comentários Godoc;
- ausência de exemplos executáveis para os principais fluxos da biblioteca.

Os pacotes prioritários são serviço, modelo, comando, protocolo, parser, sessão, estado, configuração, logging, serial, worker e utilitários.

## Resultado

`REPROVADA`

A regra está adequadamente registrada na SPEC, nas tarefas e no plano de implementação, mas a implementação atual ainda não atende integralmente RF-017. A aprovação depende da documentação do código existente e de toda implementação futura, seguida de inspeção e evidência em `validation.md`.
