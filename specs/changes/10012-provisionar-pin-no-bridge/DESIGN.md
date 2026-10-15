# DESIGN — 10012

Reutilizar o launcher da 10011 e seu parser PIN. Introduzir ramo de preparação
de arquivos no mesmo script, compartilhando validação/perfil/provisionamento/hash
com o ramo completo. Bloquear COM/toolchain/build/processos/Activity somente
no ramo completo; ValidateOnly retorna antes de qualquer escrita.

Helper reverse adiciona switch opt-in e encaminha serial, SDK derivado do ADB
resolvido e porta TCP ao launcher. BAT passa o switch e avisa se helper falhar.
Bridge permanece transporte opaco, sem conhecer propriedades PIN ou Android.

Não remover exigência de PIN do preflight Kotlin, embutir arquivo no APK ou
adotar índice/método fixo para ocultar ausência. Ambiente externo e arquivo
privado Android são fronteiras diferentes: listener não transfere configurações.
