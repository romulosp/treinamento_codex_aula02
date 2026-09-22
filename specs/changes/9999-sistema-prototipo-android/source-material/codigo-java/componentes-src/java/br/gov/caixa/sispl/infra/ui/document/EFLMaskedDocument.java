package br.gov.caixa.sispl.infra.ui.document;

public class EFLMaskedDocument implements EFLDocument {

	protected enum CharType {
		ALFA(1),
		ALFANUMERIC(2),
		NUMERIC(3);
		
		private int cod;
		
		private CharType(int cod) {
			this.cod = cod;
		}
		
		public int getCod() {
			return cod;
		}
	}
	
	protected static int TEXT_LENGTH;
	protected static int MASKEDTEXT_LENGTH;
	protected String text;
	protected int cursorPosition;
	protected int maxLength;
	protected CharType validCharType;
	
	public EFLMaskedDocument(int textLength, int maskedTextLength, CharType validCharType) {
		this.validCharType = validCharType;
		text = "";
		TEXT_LENGTH = textLength;
		MASKEDTEXT_LENGTH = maskedTextLength;
		maxLength = TEXT_LENGTH;
	}

	/**
	 * Método que verifica se o caracter digitado é válido conforme a restrição do campo,
	 * determinada pelo atributo <b>validCharType</b>.
	 * @param key
	 * @return
	 */
	private boolean isValidChar(char key) {
		switch (getValidCharType()) {
		case ALFA:
			return Character.isLetter(key);
		case NUMERIC:
			return Character.isDigit(key);
		default:
			return Character.isLetterOrDigit(key);
		}
	}
	
	@Override
	public void insertTextAtCursor(String keyText) {
		if ((keyText.length() == 1) && (getMaskedText().length() < getMaxLength())) {
			
			/** Verifica se o caracter digitado é válido.  **/
			if (!isValidChar(keyText.charAt(0))) {
				return;
			}
			
			text = text.substring(0, cursorPosition) + keyText + text.substring(cursorPosition,text.length());
			setCursorPosition(cursorPosition + keyText.length());
        }
	}

	@Override
	public void deleteCharBeforeCursor() {
		if (cursorPosition > 0) {
            text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition, text.length());
            setCursorPosition(cursorPosition - 1);
        }
	}

	@Override
	public int getCursorPosition() {
		return cursorPosition;
	}

	@Override
	public void setCursorPosition(int cursorPosition) {
		if (cursorPosition > text.length()) {
            this.cursorPosition = text.length();
        } else if (cursorPosition >= 0) {
            this.cursorPosition = cursorPosition;
        }
	}

	@Override
	public void setText(String text) {
		if ((maxLength != 0) && (text.length() > maxLength)) {
            this.text = text.substring(0, maxLength);
        } else {
            this.text = text;
        }
        setCursorPosition(cursorPosition);
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getMaskedText() {
		return text;
	}

	@Override
	public boolean isFinished() {
		return (text.length() == TEXT_LENGTH);
	}

	@Override
	public int getMinLength() {
		return MASKEDTEXT_LENGTH;
	}

	@Override
	public int getMaxLength() {
		return MASKEDTEXT_LENGTH;
	}

	public CharType getValidCharType() {
		return validCharType;
	}

	public void setValidCharType(CharType charType) {
		this.validCharType = charType;
	}

}
