# Plano — 10014

Alterar apenas core Go e testes. Criar payment_authorization.go com clone/clear
de GTK, snapshot privado e consulta; integrar buffers em payment.go. Testar
os quatro modos, pós-close/invalidação, sem alias e JSON sem campos.
Executar Go all/vet/integration; regenerar AAR e reconstruir host/plugin porque
o comportamento core é embutido no Android. Validar proveniência e instalar
somente artefato verificado, mantendo listener físico existente.
Hardware, race com CGO e auditoria independente seguem gates pendentes.
