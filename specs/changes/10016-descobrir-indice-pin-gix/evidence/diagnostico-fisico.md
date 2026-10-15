# Tentativa de descoberta física — 2026-10-14

Comando: `go run ./build/10016-key-discovery`, diretório móduloGo; fonte
[query-discovered-pin.go.txt](query-discovered-pin.go.txt) copiada temporariamente.
Programa apenas Open/ResolveGIX/cache/Close, sem GCX/GTK/GOX/GPN/captura.
Fonte PIN lida integralmente pelo parser, nunca valores secretos.

Saída: `open_failed SERIAL_UNAVAILABLE`. Processo exit0, mas diagnóstico físico
FALHOU antes de OPN/GIX; não confundir saída do executável com aceite do hardware.
Nenhum índice foi selecionado nesta tentativa. Nenhuma compra realizada.

Win32_SerialPort/.NETPortNames informam COM1 e COM7 presentes/OK, sendo COM7
DispositivoSerialUSB. Listener127.0.0.1:39100 continuaPID33664. Porta existir não
prova disponibilidade de abertura. Metadados sanitizados do log não esclareceram
motivo da falha; não afirmar qual processo ocupa a porta nem encerrar processos.
Pergunta pendente ao usuário sobre utilitário de configuração/teste usandoCOM7.

GIX histórico10015 havia informado PIN02/07 presentes,08ausente. Esta tentativa
não é uma reconfirmação desse mapa. A seleção nova usa o mapa da sessão atual;
não fixar02/07 a partir do histórico.

## Reteste autorizado pelo usuário — 14-out-2026 22:12:23 America/Sao_Paulo

Mesmo comando `go run ./build/10016-key-discovery`, exit0. Desta vez abertura
física passou, GIX000, quantidade de índices PIN DUKPTTDES disponíveis=2.
API sorteou índice02, preservando método3 da fonte. Fonte legada continha08,
demonstrando substituição pelo índice obtido noGIX. Repetição resolve na mesma
sessão devolveu02 (`cachedRepeatSame=true`), sem novoGIX/sorteio. CLO000 e
`close_confirmed`; listener39100PID33664 mantido.

Nova descoberta física validada; não confirma ainda captura PINblock/KSN porque
não foram enviados GCX/GTK/GOX/GPN. Nenhum cartão/senha/compra executado.
Na próxima sessão da aplicação haverá novo sorteio entre os presentes naquele
GIX; não tratar02 como índice fixo nem usar esta seleção encerrada em nova sessão.
