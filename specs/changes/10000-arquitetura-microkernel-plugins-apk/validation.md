# Validação e evidências

## Status

`PENDENTE_ESPECIFICACAO`

Não houve implementação nem execução de comandos de build para esta Change.
Esta seção passa a ser preenchida somente depois de `SPEC_APROVADA`.

## Evidências obrigatórias após implementação

| ID | Cenário | Evidência reproduzível esperada |
| --- | --- | --- |
| VAL-01 | Build dos módulos | comando Gradle, ambiente, saída resumida e código de saída 0 |
| VAL-02 | API compartilhada | teste de ABI e inspeção do APK provando `shared-api` não duplicada |
| VAL-03 | Plugin válido | log mascarado de validação, ativação e captura da tela no Android Studio |
| VAL-04 | Assinatura inválida | teste que prova rejeição antes da criação do `DexClassLoader` |
| VAL-05 | API/manif. inválido | teste parametrizado com motivo de rejeição auditável |
| VAL-06 | Falha em callback | teste instrumentado: Core segue ativo e plugin fica `ERROR` |
| VAL-07 | Atualização ativa | teste: atualização passa a `PENDING_RESTART`, sem hot swap |
| VAL-08 | Sem plugin de login | teste: bloqueio técnico e nenhuma rota protegida disponível |
| VAL-09 | Privacidade | asserções sobre eventos e logs sem senha ou token |
| VAL-10 | Manual | roteiro no Android Studio, dispositivo/emulador, data, operador e screenshots |

## Roteiro manual proposto

1. Compilar o host e `plugin-login` em modo debug permitido.
2. Depositar o APK de teste no canal de staging autorizado.
3. Abrir o app em um tablet/emulador em paisagem no Android Studio.
4. Confirmar que a tela de identificação aparece com campos editáveis e teclado
   virtual funcional.
5. Substituir o APK pelo candidato de versão posterior e confirmar o estado
   `PENDING_RESTART`; fechar e abrir o app para confirmar a nova ativação.
6. Repetir com assinatura e manifesto inválidos, confirmando bloqueio anterior
   ao carregamento e ausência de segredos nos logs.
