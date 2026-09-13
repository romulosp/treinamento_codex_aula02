package state

const (
	StatusOK                                  = "000"
	StatusNoSecurity                          = "003"
	StatusF1                                  = "004"
	StatusF2                                  = "005"
	StatusF3                                  = "006"
	StatusF4                                  = "007"
	StatusBackspace                           = "008"
	StatusPacketSecurityError                 = "009"
	StatusInvalidCall                         = "010"
	StatusInvalidParameter                    = "011"
	StatusTimeout                             = "012"
	StatusCancel                              = "013"
	StatusMandatoryParameter                  = "019"
	StatusTableVersionDifferent               = "020"
	StatusTableError                          = "021"
	StatusInternalError                       = "040"
	StatusMagneticCardDataError               = "041"
	StatusKeyError                            = "042"
	StatusNoCard                              = "043"
	StatusPINBusy                             = "044"
	StatusResponseOverflow                    = "045"
	StatusCryptographicError                  = "046"
	StatusNoSAM                               = "051"
	StatusDumbCard                            = "060"
	StatusCardError                           = "061"
	StatusCardApplicationInvalidated          = "067"
	StatusCardProblems                        = "068"
	StatusCardInvalidData                     = "069"
	StatusCardApplicationNotAllowed           = "070"
	StatusCardApplicationNotAuthorized        = "071"
	StatusFallbackError                       = "076"
	StatusInvalidAmount                       = "077"
	StatusMaximumAIDError                     = "078"
	StatusCardBlocked                         = "079"
	StatusContactlessMultipleCards            = "080"
	StatusContactlessCommunicationError       = "081"
	StatusContactlessInvalidated              = "082"
	StatusContactlessProblems                 = "083"
	StatusContactlessApplicationNotAllowed    = "084"
	StatusContactlessApplicationNotAuthorized = "085"
	StatusContactlessExternalCVM              = "086"
	StatusContactlessInterfaceChange          = "087"
	StatusMultimediaFileNotFound              = "100"
	StatusMultimediaFormatError               = "101"
	StatusMultimediaError                     = "102"
)

var statusDescriptions = map[string]string{
	StatusOK: "Sucesso", StatusNoSecurity: "Sem segurança", StatusF1: "Tecla F1 pressionada",
	StatusF2: "Tecla F2 pressionada", StatusF3: "Tecla F3 pressionada", StatusF4: "Tecla F4 pressionada",
	StatusBackspace: "Tecla LIMPA pressionada", StatusPacketSecurityError: "Erro de segurança do pacote",
	StatusInvalidCall: "Chamada inválida", StatusInvalidParameter: "Parâmetro inválido", StatusTimeout: "Tempo esgotado",
	StatusCancel: "Operação cancelada", StatusMandatoryParameter: "Parâmetro obrigatório ausente",
	StatusTableVersionDifferent: "Versão diferente da tabela", StatusTableError: "Erro na tabela",
	StatusInternalError: "Erro interno", StatusMagneticCardDataError: "Erro nos dados magnéticos",
	StatusKeyError: "Erro de chave", StatusNoCard: "Nenhum cartão presente", StatusPINBusy: "PIN ocupado",
	StatusResponseOverflow: "Resposta excedeu o limite", StatusCryptographicError: "Erro criptográfico",
	StatusNoSAM: "SAM não disponível", StatusDumbCard: "Cartão sem suporte", StatusCardError: "Erro no cartão",
	StatusCardApplicationInvalidated: "Aplicação ICC invalidada", StatusCardProblems: "ICC inválido ou com problemas",
	StatusCardInvalidData: "ICC com dados inválidos", StatusCardApplicationNotAllowed: "Modo inválido para ICC",
	StatusCardApplicationNotAuthorized: "ICC não aceito", StatusFallbackError: "Erro sujeito a fallback",
	StatusInvalidAmount: "Valor inválido", StatusMaximumAIDError: "Erro no limite de AID", StatusCardBlocked: "Cartão bloqueado",
	StatusContactlessMultipleCards: "Múltiplos cartões contactless", StatusContactlessCommunicationError: "Erro de comunicação contactless",
	StatusContactlessInvalidated: "Contactless invalidado ou bloqueado", StatusContactlessProblems: "Contactless inválido",
	StatusContactlessApplicationNotAllowed: "Modo inválido para contactless", StatusContactlessApplicationNotAuthorized: "Contactless não aceito",
	StatusContactlessExternalCVM: "Verificar no dispositivo do portador", StatusContactlessInterfaceChange: "Mudar interface",
	StatusMultimediaFileNotFound: "Arquivo multimídia não encontrado", StatusMultimediaFormatError: "Formato multimídia inválido",
	StatusMultimediaError: "Erro multimídia",
}

func GetStatusDescription(status string) string {
	if description, ok := statusDescriptions[status]; ok {
		return description
	}
	return "Código desconhecido"
}
