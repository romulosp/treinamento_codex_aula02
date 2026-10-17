package br.gov.caixa.sispl.infra.ui.document;

public class EFLNumeroContratoDocument extends EFLMaskedDocument {

	private static final int NUMEROCONTRATOTEXT_LENGTH = 13;
	private static final int MASKEDNUMEROCONTRATOTEXT_LENGTH = 14;
	
	public EFLNumeroContratoDocument() {
		super(NUMEROCONTRATOTEXT_LENGTH, MASKEDNUMEROCONTRATOTEXT_LENGTH, CharType.NUMERIC);
	}
	
	public int getCursorPosition() {
		int cursorPosition = text.length();
		cursorPosition += ((cursorPosition > 12) ? 1 : 0);
		return cursorPosition;
	}
	
	@Override
	public String getMaskedText() {
		String response;
		if (text.length() > 12) {
			response = text.substring(0,12) + "-" + text.substring(12);
		} else {
			response = text;
		}
		return (response);
	}
}
