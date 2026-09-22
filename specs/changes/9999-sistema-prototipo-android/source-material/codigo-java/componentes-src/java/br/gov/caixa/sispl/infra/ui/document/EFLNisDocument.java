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
public class EFLNisDocument implements EFLDocument {

	private static final int NISTEXT_LENGTH = 11;
	private static final int MASKEDNISTEXT_LENGTH = 14;
	private String nisText;
	private int cursorPosition;
	private int maxLength;

	/**
	 *
	 */
	public EFLNisDocument() {
		nisText = "";
		maxLength = 11;
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
	 */
	public void insertTextAtCursor(String keyText) {
		if ((keyText.length() == 1) && (getMaskedText().length() < getMaxLength())) {
			char numberChar = keyText.charAt(0);
			if ((numberChar >= '0') && (numberChar <= '9')) {
				nisText += numberChar;
			}
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
	 */
	public void deleteCharBeforeCursor() {
		if (nisText.length() > 0) {
			nisText = nisText.substring(0, nisText.length() - 1);
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
	 */
	public int getCursorPosition() {
		cursorPosition = nisText.length();
		cursorPosition += ((cursorPosition > 3) ? 1 : 0) + ((cursorPosition > 8) ? 1 : 0) + ((cursorPosition > 10) ? 1 : 0);
		return cursorPosition;
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setCursorPosition(int)
	 */
	public void setCursorPosition(int cursorPosition) {
		// Não faz nada.
	}


	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setText(java.lang.String)
	 */
	public void setText(String text) {
		if ((maxLength != 0) && (text.length() > maxLength)) {
			this.nisText = text.substring(0, maxLength);
		} else {
			this.nisText = text;
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
	 */
	public String getText() {
		return nisText;
	}

	public String getMaskedText() {
		//999.99999.99-9
		String response;
		if (nisText.length() > 10) {
			response = nisText.substring(0,3) + "." + nisText.substring(3,8) + "." + nisText.substring(8,10) + "-" + nisText.substring(10);
		}else if(nisText.length() > 8){
			response = nisText.substring(0,3) + "." + nisText.substring(3,8) + "." + nisText.substring(8);
		}else if(nisText.length() > 3){
			response = nisText.substring(0,3) + "." + nisText.substring(3);
		}else{
			response = nisText;
		}
		return (response);
    }

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
	 */
	public boolean isFinished() {
		return (nisText.length() == NISTEXT_LENGTH);
	}

	public int getMinLength() {
		return MASKEDNISTEXT_LENGTH;
	}

	public int getMaxLength() {
		return MASKEDNISTEXT_LENGTH;
	}
}