# Arquitetura e perfis

## Seleção de perfil

| Perfil | Use quando | Padrão inicial |
| --- | --- | --- |
| SIMPLE | Poucas telas, equipe pequena, baixo risco | Um módulo `app`, UI/data packages, DI manual |
| STANDARD | Vários fluxos, persistência/rede, evolução contínua | UI/data, repositories, UDF, testes; Hilt se reduzir complexidade |
| ENTERPRISE | Muitas features/equipes, contratos e builds independentes | Módulos por feature/capacidade, core mínimo, fitness functions |
| HIGH_ASSURANCE | Dados/regulação/risco elevado | Threat model, supply chain, privacidade e revisão independente |

Escolha o menor perfil suficiente. Registre evidência antes de subir o nível.

## Baseline

- Single-activity e Compose para novas interfaces.
- UI state imutável produzido por state holders/ViewModels.
- UDF: eventos sobem, estado desce.
- Data layer com repositories e fonte de verdade explícita.
- Domain layer apenas para lógica reutilizada ou complexa.
- Coroutines/Flow entre camadas; coleta lifecycle-aware.
- Dependências por construtor e contratos pequenos.
- UI adaptativa por window size e capacidade, preservando estado em resize e rotação.

## Decisões contextuais

Exigem justificativa na SPEC/ADR: multi-module, Hilt, Room, DataStore, WorkManager, Navigation 3, offline-first, sincronização, KMP, NDK, dynamic features e frameworks não oficiais.

Não fixe versão de AGP, Kotlin, Compose, SDK ou biblioteca nesta referência. Consulte documentação oficial no momento da Change e registre a versão escolhida.
