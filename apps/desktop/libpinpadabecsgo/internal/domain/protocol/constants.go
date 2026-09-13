package protocol

import "time"

// PP_DC2 identifica PKTDATA protegido pela Comunicação Segura ABECS.
const PP_DC2 byte = 0x12

// Limites e prazos do nível de enlace ABECS 2.12.
const (
	PacketDataMaxSize      = 2049
	CommandDataMaxSize     = 2044
	MaxAttempts            = 3
	AcknowledgementTimeout = 2 * time.Second
	ResponseTimeout        = 10 * time.Second
)

// ResponseTag identifies a RSP_DATID value returned by the pinpad.
type ResponseTag uint16

// Tags fixas RSP_DATID conhecidas pela biblioteca.
const (
	TagSerialNumber               ResponseTag = 0x8001
	TagPartNumber                 ResponseTag = 0x8002
	TagModel                      ResponseTag = 0x8003
	TagManufacturer               ResponseTag = 0x8004
	TagCapabilities               ResponseTag = 0x8005
	TagOSVersion                  ResponseTag = 0x8006
	TagSpecification              ResponseTag = 0x8007
	TagManufacturerVersion        ResponseTag = 0x8008
	TagApplicationVersion         ResponseTag = 0x8009
	TagGeneralVersion             ResponseTag = 0x800A
	TagContactlessCapabilities    ResponseTag = 0x800B
	TagKernelVersion              ResponseTag = 0x8010
	TagContactlessVersion         ResponseTag = 0x8011
	TagMagneticContactlessVersion ResponseTag = 0x8012
	TagVisaContactlessVersion     ResponseTag = 0x8013
	TagAmexContactlessVersion     ResponseTag = 0x8014
	TagDiscoverContactlessVersion ResponseTag = 0x8015
	TagPureVersion                ResponseTag = 0x8016
	TagQPContactlessVersion       ResponseTag = 0x8018
	TagDisplayTextSize            ResponseTag = 0x8020
	TagDisplayGraphicSize         ResponseTag = 0x8021
	TagMultimediaSupport          ResponseTag = 0x8022
	TagMerchantDescription        ResponseTag = 0x8032
	TagMerchantData               ResponseTag = 0x8033
	TagDukptTDESDescription       ResponseTag = 0x8035
	TagDukptTDESData              ResponseTag = 0x8036
	TagEvent                      ResponseTag = 0x8040
	TagTrack1Included             ResponseTag = 0x8041
	TagTrack2Included             ResponseTag = 0x8042
	TagTrack3Included             ResponseTag = 0x8043
	TagTrack1                     ResponseTag = 0x8044
	TagTrack2                     ResponseTag = 0x8045
	TagTrack3                     ResponseTag = 0x8046
	TagTrack1KSN                  ResponseTag = 0x8047
	TagTrack2KSN                  ResponseTag = 0x8048
	TagTrack3KSN                  ResponseTag = 0x8049
	TagEncryptedPAN               ResponseTag = 0x804A
	TagEncryptedPANKSN            ResponseTag = 0x804B
	TagKSN                        ResponseTag = 0x804C
	TagValue                      ResponseTag = 0x804D
	TagDataOut                    ResponseTag = 0x804E
	TagCardType                   ResponseTag = 0x804F
	TagICCStatus                  ResponseTag = 0x8050
	TagAIDTableInfo               ResponseTag = 0x8051
	TagPAN                        ResponseTag = 0x8052
	TagPANSequenceNumber          ResponseTag = 0x8053
	TagEMVData                    ResponseTag = 0x8054
	TagCardholderName             ResponseTag = 0x8055
	TagGOXResult                  ResponseTag = 0x8056
	TagPINBlock                   ResponseTag = 0x8057
	TagFCXResult                  ResponseTag = 0x8058
	TagISResults                  ResponseTag = 0x8059
	TagBigRandom                  ResponseTag = 0x805A
	TagLabel                      ResponseTag = 0x805B
	TagIssuerCountry              ResponseTag = 0x805C
	TagCardExpiration             ResponseTag = 0x805D
	TagMultimediaFileName         ResponseTag = 0x805E
	TagDeviceType                 ResponseTag = 0x8060
	TagTLRMemory                  ResponseTag = 0x8062
	TagEncryptedRandom            ResponseTag = 0x8063
	TagBatteryInfo                ResponseTag = 0x8064
	TagCommunicationInfo          ResponseTag = 0x8065
)

// Bases das famílias dinâmicas de tags retornadas por GIX.
const (
	TagKSNTDESDescriptionBase ResponseTag = 0x9100
	TagKSNTDESDataBase        ResponseTag = 0x9200
	TagTableVersionBase       ResponseTag = 0x9300
)

// TagKSNTDESDescription calcula a tag de descrição KSN TDES pelo índice.
func TagKSNTDESDescription(index byte) ResponseTag {
	return TagKSNTDESDescriptionBase + ResponseTag(index)
}

// TagKSNTDESData calcula a tag de dados KSN TDES pelo índice.
func TagKSNTDESData(index byte) ResponseTag { return TagKSNTDESDataBase + ResponseTag(index) }

// TagTableVersion calcula a tag de versão de tabela pelo índice do adquirente.
func TagTableVersion(index byte) ResponseTag { return TagTableVersionBase + ResponseTag(index) }

// Tipos de mídia e limites de transferência definidos pelo ABECS 2.12.
const (
	MediaTypeRUF    byte = 0x00
	MediaTypePNG    byte = 0x01
	MediaTypeJPG    byte = 0x02
	MediaTypeGIF    byte = 0x03
	MLRMaxBlockSize      = 995
	TLRMaxRecords        = 99
)
