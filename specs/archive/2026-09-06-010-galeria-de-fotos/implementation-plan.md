# Plano de implementação — 010-galeria-de-fotos

## Pré-condição

A SPEC foi aprovada em `reviews/2026-09-06-spec-review.md` antes do início da implementação.

## Impactos

- Criação local do módulo Maven `apps/backend/fotogaleria/`.
- Criação local do módulo Vite `apps/frontend/web/fotogaleria/`.
- Adição posterior do estado vigente da Galeria em `specs/system/`.
- Preservação da política Git que ignora código gerado em `apps/`.

## Estratégia

1. Criar o backend Quarkus com dependências REST Jackson, Hibernate ORM Panache, PostgreSQL e Validation.
2. Implementar modelo e porta no domínio, serviço na aplicação, entidade/repositório na infraestrutura e DTOs/recurso na API.
3. Criar testes unitários do serviço com JUnit 5, Mockito e JaCoCo.
4. Criar frontend React/Vite com Material UI, componentes focados e serviço Fetch.
5. Criar testes do serviço HTTP e dos principais fluxos visuais com Vitest/Testing Library.
6. Criar gerador PowerShell orientado pela seção do projeto, template sem segredos, scripts `.bat` e Docker Compose; testar o parser com arquivo sintético sem iniciar processos persistentes.

## Testes e qualidade

- Backend: `mvn test` e `mvn verify` se Java/Maven estiverem disponíveis; caso contrário, inventário e auditoria estática complementar com a indisponibilidade registrada.
- Frontend: `npm test -- --run` e `npm run build`.
- Segurança: busca por segredos, HTML cru, `eval`, URLs absolutas no cliente e validação das entradas de upload.
- Artefatos gerados (`target`, `dist`, `node_modules`) não serão versionados.

## Inventário Java planejado

Classes com lógica aplicável: `FotoService`, `FotoResource` e `FotoRepositoryPanache`. DTOs declarativos, entidade JPA, modelo simples, contrato de repositório e mapeadores de exceção serão inventariados e excluídos de teste unitário somente com justificativa. O serviço terá cobertura unitária direta; HTTP e Panache exigiriam container e pertencem a integração.

## Riscos e mitigação

- **Java/Maven ausentes:** registrar comandos não executáveis, versões detectadas e revisão estática; não declarar cobertura numérica.
- **Dependências npm indisponíveis:** registrar falha objetiva de instalação e executar inspeção estática disponível.
- **Dados grandes:** limite de 5 MiB no serviço e no transporte; lista sem Base64.
- **API desprotegida:** restringir documentação a uso local e abrir recomendação de Change separada na auditoria.
- **Processos locais persistentes:** validar configuração e builds sem deixar servidores ou containers ativos após a execução automatizada.
