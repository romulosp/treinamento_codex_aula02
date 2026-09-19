# Workflow Android orientado por SPEC

## Discovery

Defina problema, usuários, fluxos, dispositivos, form factors, offline, dados, integrações, distribuição e riscos. Separe requisito de preferência técnica.

## SPEC

Inclua comportamento verificável, perfil, arquitetura, módulos, package name, SDKs decididos com fonte e data, contratos, segurança, privacidade, acessibilidade, testes e critérios de aceite. Registre decisões arquiteturais que alterem limites ou stack.

## Implementação

Implemente vertical slices pequenos. Preserve decisões aprovadas. Um spike deve ficar identificado como descartável e não pode ser promovido silenciosamente.

## Revisão e validação

Compare código com SPEC, execute quality gates e registre evidências. Validação em dispositivo é obrigatória quando sensores, lifecycle, permissões, performance ou UI real não podem ser comprovados em JVM.

## Recuperação

Classifique a falha, formule hipótese, faça uma correção por vez e repita somente quando nova evidência justificar. Mudança de arquitetura retorna para SPEC/ADR.
