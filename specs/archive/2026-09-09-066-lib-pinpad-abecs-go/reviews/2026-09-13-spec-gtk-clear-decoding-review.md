# Revisão de SPEC — decodificação das trilhas GTK em claro

**Data:** 2026-09-13

**Estado de entrada:** `EM_REVISAO_SPEC`

## Evidência normativa e física

A seção 5.4.2.1 do manual ABECS 2.12 define trilha 1 como ASCII. A seção
5.4.2.2 define PAN e trilhas 2/3 com um símbolo por nibble, usa `D` para o
separador apresentado como `=` no exemplo e `F` como filler final.

O valor físico observado para `PP_TRACK2` contém bytes compactados e, por isso,
foi escapado como `\xNN` quando tratado incorretamente como string. A conversão
deve ocorrer somente para o modo GTK em claro; dados de métodos criptografados
continuam opacos e redigidos.

## Decisão

Os símbolos aceitos, o tratamento do filler e a rejeição de entrada ambígua
formam um contrato determinístico e cobrem o rastro observado.

`SPEC_APROVADA`
