# Diagnóstico físico — GOX042

Pedido: usuário testou R$21,07 em14-out e continuou vendo PINPAD_ERROR.
Consulta somente de logs/metadados; não houve captura financeira automatizada.

## Tentativa real do usuário

Correlação `c93e59ea-1d77-40a8-978b-753c65bf95a6`.
08:29:33 (America/Sao_Paulo), Android telemetry:

| Comando | Status | Fase |
| --- | --- | --- |
| GCX | 000 | capture |
| GTK | 000 | gtk |
| GOX | 042 | gox |
| CLO | 000 | close |

GTK em claro passou. GOX chegou ao processamento PIN e recusou a seleção
criptográfica. Manual local ABECS2.12p30:042=ST_ERRKEY; p137:método3 é DUKPTTDESPIN;
p261:emGOX chave solicitada ausente/corrompida quando PINonline requerido.
Não se trata do GTK011 anterior. Nenhum PINblock/KSN real retornou nesta tentativa.

## Seleção efetiva e confirmação atual

ADB run-as leu o snapshot privado criptografia.properties, sem imprimir WK:
método3, índice08, WKvazia. Fonte é o properties externo, não constante de operação.
Endpoint39100 continua listenerPID33664, COM7/19200 conforme ambiente registrado.

Diagnóstico temporário `go run ./build/10015-key-metadata` no móduloGo, exit0,
às08:31:57: somente OPN/GIX/CLO, sem GCX/GTK/GOX/GPN/captura. Parser integral
do arquivo externo selecionou índice consultado; valores de chaves/KSN nunca lidos.
GIX000:tag8035(PIN DUKPTTDES), índice08=`0` ausente; presentes02/07.
Tag8036(DATA DUKPTTDES), índice08=`0` ausente; nenhuma DATA presente.
Mapa A100 validado com flags0/1/2 conforme manualp40; CLO000 confirmado.

Conclusão verificável: aplicação referencia PIN DUKPTTDES08 e o dispositivo
informa essa posição sem chave. O properties seleciona registro; não injeta chave.
GTK em claro dispensaDATA, mas não dispensaPIN quando kernel exige PINonline.

## Dependência externa para concluir o PIN

Responsável deve carregar/confirmar a chave PIN DUKPTTDES autorizada na posição08,
ou informar outro índice autorizado para atualizar a fonte properties e reprovisionar.
Não selecionar02/07 automaticamente: presença não comprova que sejam da rede/ambiente
corretos. Não alterar método, instalar chaves, fabricar retorno ou refazer compra.
Após ajuste autorizado: nova jornada do usuário para obter PINblock+KSN real.
