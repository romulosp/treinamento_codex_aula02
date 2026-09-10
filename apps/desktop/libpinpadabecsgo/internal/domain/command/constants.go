package command

// SPEParameter identifies a command parameter (SPE_xxx).
type SPEParameter uint16

const (
	SPEIDList              SPEParameter = 0x0001
	SPEPinMethod           SPEParameter = 0x0002
	SPEDataMethod          SPEParameter = 0x0003
	SPETagList             SPEParameter = 0x0004
	SPEEMVData             SPEParameter = 0x0005
	SPECEXOption           SPEParameter = 0x0006
	SPETracks              SPEParameter = 0x0007
	SPEOpenDigits          SPEParameter = 0x0008
	SPEKeyIndex            SPEParameter = 0x0009
	SPEEncryptedWorkingKey SPEParameter = 0x000A
	SPEMessageIndex        SPEParameter = 0x000B
	SPETimeout             SPEParameter = 0x000C
	SPEMinimumDigits       SPEParameter = 0x000D
	SPEMaximumDigits       SPEParameter = 0x000E
	SPEDataIn              SPEParameter = 0x000F
	SPEAcquirerReference   SPEParameter = 0x0010
	SPEApplicationType     SPEParameter = 0x0011
	SPEAIDList             SPEParameter = 0x0012
	SPEAmount              SPEParameter = 0x0013
	SPECashback            SPEParameter = 0x0014
	SPETransactionDate     SPEParameter = 0x0015
	SPETransactionTime     SPEParameter = 0x0016
	SPEGCXOption           SPEParameter = 0x0017
	SPEGOXOption           SPEParameter = 0x0018
	SPEFCXOption           SPEParameter = 0x0019
	SPETerminalParameters  SPEParameter = 0x001A
	SPEDisplayMessage      SPEParameter = 0x001B
	SPEARC                 SPEParameter = 0x001C
	SPEIVCBC               SPEParameter = 0x001D
	SPEMultimediaFileName  SPEParameter = 0x001E
	SPEMultimediaFileInfo  SPEParameter = 0x001F
	SPEMenuOption          SPEParameter = 0x0020
	SPETransactionType     SPEParameter = 0x0021
	SPETransactionCurrency SPEParameter = 0x0022
	SPEPANMask             SPEParameter = 0x0023
	SPEPinBlockMode        SPEParameter = 0x0024
	SPEPinBlockExponent    SPEParameter = 0x0025
	SPEGCDOption           SPEParameter = 0x0026
)

const (
	GKYKeyOK           byte = 0x01
	GKYKeyCancel       byte = 0x02
	GKYKeyClear        byte = 0x03
	GKYKeyF1           byte = 0x04
	GKYKeyF2           byte = 0x05
	GKYKeyF3           byte = 0x06
	GKYKeyF4           byte = 0x07
	GKYKeyNone         byte = 0xFF
	GKYModeWaitKey     byte = 0x00
	GKYModeClearBuffer byte = 0x01
	GKYModeGetKey      byte = 0x02
)

const (
	GPNMethodMKWKTDESPIN       byte = '1'
	GPNMethodDUKPTTDESPIN      byte = '3'
	GPNWorkingKeyEncryptedSize      = 32
	GPNPANMaxSize                   = 19
	GPNMessageSize                  = 32
	GPNMinPINLength                 = 4
	GPNResponseDataSize             = 36
	GPNPINBlockSize                 = 16
	GPNKSNSize                      = 20
)

const (
	GCXTransactionPurchase      byte = 0x00
	GCXTransactionWithdrawal    byte = 0x01
	GCXTransactionCashback      byte = 0x09
	GCXTransactionRefund        byte = 0x20
	GCXTransactionBalance       byte = 0x30
	GCXOptionWaitMagCard        byte = 0x00
	GCXOptionWaitContactless    byte = 0x10
	GCXOptionShowAmount         byte = 0x00
	GCXOptionHideAmount         byte = 0x08
	GCXCardMagnetic                  = "00"
	GCXCardICC                       = "03"
	GCXCardContactlessSimulated      = "05"
	GCXCardContactlessEMV            = "06"
	GCXICCStatusSuccess         byte = '0'
	GCXICCStatusFallback        byte = '1'
	GCXICCStatusUnsupported     byte = '2'
)
