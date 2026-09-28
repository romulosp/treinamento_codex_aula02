# DESIGN — 069 diagnosticopinpad Functional Lab

## Perfil

Android `STANDARD` sobre o módulo `app` já existente: a quantidade de fluxos e
formulários deixou de ser compatível com uma tela de seis botões, mas ainda não
justifica múltiplos módulos. A UI continua Compose, UDF, ViewModel e DI manual.

## Estrutura

```text
DiagnosticScreen
  ├── ConnectionSection
  ├── DisplaySection
  ├── TransactionSection
  ├── EmvPinSection
  └── MediaSection
       ↓ eventos tipados
DiagnosticViewModel / StateFlow
       ↓ repository
GoMobileRepository
       ↓ AAR
mobile.Client (fachada nomeada)
       ↓
Service existente → PBRG → Bridge → PORTA_PINPAD
```

Os formulários avançados usam uma configuração de ação em memória e um
`ActionDialog` reutilizável. A configuração não será persistida. O ViewModel
mantém uma operação por vez e o resultado de cada ação em texto sanitizado.

## Decisões

### D-069-01 — Nova Change, não alteração retroativa da 068

O catálogo completo é uma expansão funcional material. A 068 permanece como
laboratório mínimo e histórico; a 069 depende de sua fachada e transporte.

### D-069-02 — Fachada nomeada

Cada ação tem método próprio e contrato próprio. Isso preserva segurança,
tipagem, documentação e rastreabilidade, sem transformar o app em executor
ABECS raw.

### D-069-03 — Resumos redigidos

O binding converte modelos internos em resumos allowlist. PIN, trilhas, chaves,
KSN, PAN e EMV bruto são consumidos somente no Go e descartados após a operação.

### D-069-04 — Uma tela com seções

O laboratório mantém uma Activity para reduzir ciclo de build, mas usa seções,
diálogos e estados de habilitação para comportar as 28 opções sem replicar
arquitetura.

### D-069-05 — Opções impossíveis permanecem visíveis

As opções 6 e 25 fazem parte do contrato visual e serão mostradas com a razão
de indisponibilidade. Isso evita afirmar que um fluxo não implementado foi
executado.

## Estratégia de testes

- Go: testes de fachada para cada método, validação de conversões hex, resumos
  redigidos, cancelamento e transcript roteirizado;
- Kotlin JVM: reducer/ViewModel, validação de formulários, habilitação das 28
  ações e redaction;
- instrumentado: diálogo, rotação/lifecycle e saída segura;
- integração: Ping/Open/GetInfo/Close e um cenário por grupo usando Bridge;
- hardware: opções dependentes do pinpad somente com `PORTA_PINPAD` configurada.
