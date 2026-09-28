# SPEC: padronizar plugins de negócio Android

## Status
`SPEC_APROVADA`

## Requisitos

1. DEVE existir `specs/shared/architecture/android-business-plugin.md` como regra normativa de todos os `IPluginNegocioApp`.
2. DEVE existir template reutilizável em `specs/templates/business-plugin-spec-template.md`.
3. O padrão DEVE proibir Activity launcher no plugin e exigir teste humano exclusivamente pelo host após login.
4. O padrão DEVE exigir isolamento do host, serviço determinístico, Compose reutilizável, segurança de credenciais e quality gates reproduzíveis.

## Critérios de aceite

- O padrão cobre descoberta, menu, abertura e retorno dentro do microkernel.
- O template referencia o padrão compartilhado.
- Nenhuma regra exige dependência do plugin no host.
