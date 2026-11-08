# Evidências técnicas — 10018

Data: 2026-10-15 (America/Sao_Paulo). Ambiente: Windows/PowerShell.
Estado: IMPLEMENTADA; sem validação formal, aprovação final ou commit.

- Copy-Item -LiteralPath <origem temporária> -Destination <docs ABECS>/abecs212.txt: concluído, execução exit 0; origem preservada.
- Get-FileHash -Algorithm SHA256 na origem e destino: iguais, execução exit 0.
- SHA-256: 64661E6BEC556B7DABED2A98759831F0CDFA8A2A21D6722E31CD08D89C55205E.
- Tamanho preservado: 569944 bytes.
- Busca global rg -n -i --hidden -g '!.git/**' 'Temp[/\\]abecs212|AppData.*abecs212' .: exit 1, nenhuma referência externa remanescente.
- Destino dos links relativos da referência de protocolo e da memória 10017 confirmado existente; execução exit 0.

Alterados: referência de protocolo e memória da 10017. Adicionado: extração integral em docs e registro desta Change.
Não se aplicam testes de aplicação: somente cópia documental e links; nenhum conteúdo de protocolo foi alterado.