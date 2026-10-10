# Perfil de homologação DES — 10004

## Decisão e confidencialidade

Em 2026-10-10, o solicitante escolheu homologação com pinpad físico e cartão
de teste, usando `D:\desenvolvimento\chave_des\emv_des.json`.
O arquivo PRD também informado não foi acessado. Não há troca automática para
PRD nem seleção de produção na UI do operador nesta entrega.

O arquivo foi lido somente para análise estrutural. Nenhum registro, chave,
módulo CAPK, AID completo ou conteúdo bruto foi copiado para a Change,
stdout, fixtures ou APK. DES é ambiente, não método de criptografia PIN.

## Perfil PIN confirmado pelo solicitante

- Método: DUKPT TDES, código ABECS `3`.
- Índice da chave: `08` (inteiro 8 na fachada gomobile).
- `workingKey = ""`; a aplicação não fornece WK de MK/WK.
- A configuração não instala ou comprova existência da chave no pinpad.
  O teste físico deve confirmar o slot provisionado; chave ausente permanece
  erro real, sem substituição por outro índice ou método.

Em GOX, usar `pinMethod = "3"`, `keyIndex = 8`, `workingKeyHex = ""`.
Na tarja, usar a fachada `CapturePINDUKPT`, que não recebe workingKey e
seleciona o método 3. Não chamar a variante `CapturePINMK` com WK vazia.
O bloco zerado exigido pelo formato GPN em DUKPT é produzido pelo core;
não constitui uma chave de trabalho fornecida pela UI.

## Estrutura observada

| Propriedade | Resultado |
| --- | --- |
| Formato | Comentários de linha antes de um array JSON de strings. |
| Quantidade | 36 registros: 15 tipo 1/AID e 21 tipo 2/CAPK. |
| Codificação dos registros | Todos representáveis em Latin-1. |
| Tamanho TAB_LEN | Os 36 prefixos N3 correspondem ao comprimento completo, incluindo o próprio prefixo. |
| Comprimentos completos | De 284 a 611 bytes. |
| Adquirentes | Um identificador distinto; campo N2 sintaticamente válido. Valor não publicado. |
| Configuração de PIN | Não há campos nomeados de método/índice/provisionamento PIN. CAPK é chave pública EMV. |

PowerShell `ConvertFrom-Json` estrito rejeitou os comentários (exit 1).
Remover comentários de linha e analisar o array com Node funcionou (exit 0).
A implementação deverá aceitar somente o formato declarado, sem `eval`, e
rejeitar estrutura inválida sem expor conteúdo em mensagens de erro.

## Importação e carga a especificar

O Windows conhece o caminho externo. O core gomobile roda no Android; o
Bridge físico é transporte opaco e não interpreta arquivo de configuração.
Definir provisionamento local do perfil de homologação ao componente Go no
Android, preservando essa fronteira, sem colocar chaves de PIN em Kotlin.

Após classificar o conteúdo permitido e aprovar o provisionamento, carregar
AIDs/CAPKs via TLI/TLR/TLE, com adquirente e versão definidos e resultado
real confirmado. Carga EMV não instala automaticamente chave de PIN.

**Gap identificado:** os registros recebidos já contêm TAB_LEN. O builder
`BuildTLRCommand` da biblioteca adiciona prefixo N3 calculado a cada string.
Enviar strings completas por `LoadCompleteEMVTable` produz duplo prefixo.
Remover os três caracteres sem revisar o contrato também não basta: o builder
calcula comprimento excluindo o novo prefixo, enquanto os registros desta
tabela declaram comprimento incluindo TAB_LEN. Definir caminho tipado para
validar e preservar registros já formatados, com vetores normativos, antes
de enviar a tabela ao pinpad. Não corrigir framing no Bridge ou em Compose.

## PIN e sequência revisada

A fachada aceita PAN vazio. O
[manual ABECS 2.12](https://api.abecs.org.br/wp-content/uploads/2021/08/Pinpad-Abecs-Protocolo-de-Comunica%C3%A7%C3%A3o-e-Funcionamento-v212190411.pdf),
seções 3.3.11 e 6.5.11, permite usar o PAN retido pelo pinpad após GCX/CEX
nas condições End-to-End, antes de GTK. GTK pode retirar essa disponibilidade.
Logo, avaliar GPN antes de GTK e provar a sequência no perfil físico.
O achado anterior não prova necessidade de manter PAN no Android ou de criar
nova fachada de sessão. Alternativas só serão necessárias se as condições
normativas do perfil não forem satisfeitas.

## Configuração e evidência para execução física

- Validar fisicamente DUKPT TDES tipo 3, índice 08; referências já confirmadas,
  sem material criptográfico em resposta do usuário ou arquivos versionados.
- Versão/perfil e provisionamento privado debug conforme contrato executável.
- Cenário FCX explícito APPROVE/DENY/COMMUNICATION_ERROR no perfil.
- Limite e prazos obrigatórios; taxa definida por REV-007, não pelo perfil;
  débito em uma parcela; chave DATA GTK
  independente do índice PIN, conforme [contrato executável](contrato-implementacao-homologacao.md).

## Evidência de inspeção

Windows PowerShell 5.1 e Node local. Comando seguro abaixo executado com
exit 0; saída apenas quantitativa. O arquivo físico não foi alterado.

```powershell
@'
const fs = require('fs');
const raw = fs.readFileSync('D:/desenvolvimento/chave_des/emv_des.json','utf8');
const entries = JSON.parse(raw.split(/\r?\n/).filter(line => !line.trimStart().startsWith('//')).join('\n'));
const kinds = {};
for (const item of entries) { const k = item[3]; kinds[k] = (kinds[k] || 0) + 1; }
console.log('TAB_LEN incluindo N3 coerente: '+entries.filter(x=>/^\d{3}/.test(x)&&Number(x.slice(0,3))===Buffer.byteLength(x,'latin1')).length+'/'+entries.length);
console.log('Tipos de tabela (IDs e contagens, sem registros): '+JSON.stringify(kinds));
console.log('Latin-1 valido: '+entries.filter(x=>[...x].every(c=>c.codePointAt(0)<=255)).length+'/'+entries.length);
console.log('Comprimentos normalizados sem TAB_LEN: '+Math.min(...entries.map(x=>x.length-3))+'/'+Math.max(...entries.map(x=>x.length-3)));
console.log('Quantidade de adquirentes distintos: '+new Set(entries.map(x=>x.slice(4,6))).size);
console.log('Todos os indices adquirentes N2 validos: '+entries.every(x=>/^\d{2}$/.test(x.slice(4,6))));
'@ | node
```
