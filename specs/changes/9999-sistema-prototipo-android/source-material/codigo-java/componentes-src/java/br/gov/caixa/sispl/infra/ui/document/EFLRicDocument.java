package br.gov.caixa.sispl.infra.ui.document;

/**
 * Mascara para o campo RIC
 * 
 * @author r688989 - Natã Alves Evangelista
 */
public class EFLRicDocument implements EFLDocument {

	private static final int RICTEXT_LENGTH = 11;
	private static final int MASKEDRICTEXT_LENGTH = 12;
	private String ricText;
	private int cursorPosition;
	private int maxLength;

	/**
	 *
	 */
	public EFLRicDocument() {
		ricText = "";
		maxLength = 11;
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
	 */
	public void insertTextAtCursor(String keyText) {
		if ((keyText.length() == 1) && (getMaskedText().length() < getMaxLength())) {
			char numberChar = keyText.charAt(0);
			if ((numberChar >= '0') && (numberChar <= '9')) {
				ricText += numberChar;
			}
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
	 */
	public void deleteCharBeforeCursor() {
		if (ricText.length() > 0) {
			ricText = ricText.substring(0, ricText.length() - 1);
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
	 */
	public int getCursorPosition() {
		int cursorPosition = ricText.length();
		cursorPosition += ((cursorPosition > 10) ? 1 : 0);
		return cursorPosition;
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setCursorPosition(int)
	 */
	public void setCursorPosition(int cursorPosition) {
		//Não faz nada.
	}


	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setText(java.lang.String)
	 */
	public void setText(String text) {
		if ((maxLength != 0) && (text.length() > maxLength)) {
			this.ricText = text.substring(0, maxLength);
		} else {
	    	this.ricText = text;
	    }
		setCursorPosition(cursorPosition);
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
	 */
	public String getText() {
		return ricText;
	}

	public String getMaskedText() {
		String response;
		if (ricText.length() <= 10) {
			response = ricText;
		} else {
			response = ricText.substring(0,10) + "-" + ricText.substring(10);
		}
		return (response);
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
	 */
	public boolean isFinished() {
		return (ricText.length() == RICTEXT_LENGTH);
	}

	public int getMinLength() {
		return MASKEDRICTEXT_LENGTH;
	}

	public int getMaxLength() {
		return MASKEDRICTEXT_LENGTH;
	}
}
