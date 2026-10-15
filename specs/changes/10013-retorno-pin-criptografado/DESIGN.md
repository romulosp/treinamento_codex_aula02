# DESIGN — 10013

Core oferece CapturePaymentPINResult como operação aditiva que transfere os
buffers ao chamador; wrapper void antigo limpa. Continuação EMV já retorna blobs
privados ao Go: nova fachada os transfere a PaymentResult somente se online.
PaymentResult usa mutex e getters por cópia/Clear, não JSON.

ParsePaymentPINConfiguration expõe objeto tipado immutable com três getters.
Adapter obtém os três campos desse objeto, incluindo WK; perfil congela flag
de diagnóstico. Sem alterar SaqueRepository de UI nem estado da jornada.

Adapter usa novas APIs, extrai decisão e par, chama logger privado em IO,
limpa cópias e objeto em finally. Logger bounded noBackup/debug/DES com opt-in;
falha não invalida resultado EMV. Manifesto/UI/Bridge opaco mantidos.
