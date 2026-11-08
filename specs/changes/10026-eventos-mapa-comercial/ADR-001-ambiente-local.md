# ADR-001 — Configuração de SDK local

Decisão contextual de ambiente, 2026-11-08: conservar o local.properties preexistente necessário ao SDK desta máquina. O validador estrutural genérico rejeita sua mera presença, mesmo ignorado pelo Git. A inspeção confirmou configuração de caminho do SDK, não credencial; git check-ignore confirmou exclusão. Não remover configuração do usuário nem alterar o validador global para esta feature.

O comando original e seu exit code 1 ficam registrados como ressalva ambiental não bloqueante para teste manual local. Não declarar que o validador passou. O arquivo local não integra os APKs nem será incluído em commit. Os demais gates aplicáveis devem passar. Essa decisão não dispensa aprovação humana final, atualização de specs/system ou os gates de archive/commit.
