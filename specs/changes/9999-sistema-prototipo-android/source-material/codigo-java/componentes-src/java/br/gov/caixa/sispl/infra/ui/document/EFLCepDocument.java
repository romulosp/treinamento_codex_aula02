/*
 * Created on 04/09/2008
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.ui.document;

/**
 * @author p542991
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFLCepDocument implements EFLDocument {

	private static final int CEPTEXT_LENGTH = 8;
	private static final int MASKEDCEPTEXT_LENGTH = 9;
	private String cepText;
	private int cursorPosition;
	private int maxLength;

	/**
	 *
	 */
	public EFLCepDocument() {
		cepText = "";
		maxLength = 8;
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
	 */
	public void insertTextAtCursor(String keyText) {
		if ((keyText.length() == 1) && (getMaskedText().length() < getMaxLength())) {
			char numberChar = keyText.charAt(0);
			if ((numberChar >= '0') && (numberChar <= '9')) {
				cepText += numberChar;
			}
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
	 */
	public void deleteCharBeforeCursor() {
		if (cepText.length() > 0) {
			cepText = cepText.substring(0, cepText.length() - 1);
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
	 */
	public int getCursorPosition() {
		int cursorPosition = cepText.length();
		cursorPosition += ((cursorPosition > 5) ? 1 : 0);
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
			this.cepText = text.substring(0, maxLength);
		} else {
	    	this.cepText = text;
	    }
		setCursorPosition(cursorPosition);
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
	 */
	public String getText() {
		return cepText;
	}

	public String getMaskedText() {
		String response;
		if (cepText.length() <= 5) {
			response = cepText;
		} else {
			response = cepText.substring(0,5) + "-" + cepText.substring(5);
		}
		return (response);
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
	 */
	public boolean isFinished() {
		return (cepText.length() == CEPTEXT_LENGTH);
	}

	public int getMinLength() {
		return MASKEDCEPTEXT_LENGTH;
	}

	public int getMaxLength() {
		return MASKEDCEPTEXT_LENGTH;
	}
}
