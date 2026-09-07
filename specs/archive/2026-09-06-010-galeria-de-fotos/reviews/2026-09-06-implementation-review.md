# Revisão da implementação — 010-galeria-de-fotos

## Resultado

`REPROVADA` em 2026-09-06.

## Escopo revisado

Foram confrontados os módulos `apps/backend/fotogaleria/` e `apps/frontend/web/fotogaleria/` com `proposal.md`, `spec.md`, `DESIGN.md`, `implementation-plan.md`, `AGENTS.md` e o workflow canônico.

## Evidências executadas antes da revisão

- Backend: `mvn test`, com Java 17.0.11 e Maven 3.8.8 — 13 testes aprovados, código de saída 0.
- Frontend: `npm test -- --run` — 7 testes aprovados, código de saída 0.
- Frontend: `npm run build` — build concluído, código de saída 0.

## Achados

### IMP-REV-001 — Bloqueante

- **Evidência:** REQ-015 determina que `testar_aplicacao.bat` valide `D:\desenvolvimento\chave_des\026-chaves-aplicacao`. O script implementado define `DIRETORIO_CHAVES=D:\desenvolvimento\chave_des\chave_des.properties` e valida esse arquivo diferente.
- **Impacto:** o contrato operacional aprovado não é atendido e uma instalação que possua somente o diretório exigido pela SPEC será recusada.
- **Ação necessária:** alterar o script, na fase de implementação, para validar e informar exatamente o diretório externo definido em REQ-015, sem ler nem imprimir seu conteúdo.

### IMP-REV-002 — Bloqueante

- **Evidência:** a regra de implementação do prompt orquestrador exige `apps/backend/fotogaleria/start_aplicacao.bat` para projetos Java Quarkus gerados, preservando Java 17.0.11, Maven 3.8.8 e a inicialização por `mvn quarkus:dev`. O arquivo está ausente.
- **Impacto:** falta um artefato operacional obrigatório do processo usado para executar a Change.
- **Ação necessária:** criar o script obrigatório na fase de implementação com o conteúdo contratual do orquestrador.

## Itens aderentes observados

- A separação entre API, aplicação, domínio e infraestrutura está presente, sem exposição de entidade JPA na API.
- Os cinco endpoints, DTOs, paginação sem Base64, validações de upload e persistência PostgreSQL estão implementados.
- O frontend contém login demonstrativo, grade responsiva, paginação, upload múltiplo, edição, exclusão, lightbox, Fetch relativo e proxy `/api`.
- Docker Compose contém PostgreSQL, healthcheck e volume nomeado.
- Os testes disponíveis passaram após correções de configuração realizadas ainda na fase de implementação.

## Conclusão

Existem divergências materiais entre a implementação e o contrato. Veredito: `REPROVADA`. A primeira fase a retomar é **Implementação**. Validação, auditoria de segurança, aprovação, arquivamento e commit não estão autorizados enquanto os achados bloqueantes permanecerem abertos.
