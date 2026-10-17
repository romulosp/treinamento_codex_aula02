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
public class EFLCpfDocument implements EFLDocument {

	private static final int CPFTEXT_LENGTH = 11;
	private static final int MASKEDCPFTEXT_LENGTH = 14;
	private String cpfText;
	private int cursorPosition;
	private int maxLength;

	/**
	 *
	 */
	public EFLCpfDocument() {
		cpfText = "";
		maxLength = 11;
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
	 */
	public void insertTextAtCursor(String keyText) {
		if ((keyText.length() == 1) && (getMaskedText().length() < getMaxLength())) {
			char numberChar = keyText.charAt(0);
			if ((numberChar >= '0') && (numberChar <= '9')) {
				cpfText += numberChar;
			}
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
	 */
	public void deleteCharBeforeCursor() {
		if (cpfText.length() > 0) {
			cpfText = cpfText.substring(0, cpfText.length() - 1);
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
	 */
	public int getCursorPosition() {
		cursorPosition = cpfText.length();
		cursorPosition += ((cursorPosition > 3) ? 1 : 0) + ((cursorPosition > 6) ? 1 : 0) + ((cursorPosition > 9) ? 1 : 0);
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
			this.cpfText = text.substring(0, maxLength);
		} else {
			this.cpfText = text;
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
	 */
	public String getText() {
		return cpfText;
	}

	public String getMaskedText() {
		String response;
		if (cpfText.length() > 9) {
			response = cpfText.substring(0,3) + "." + cpfText.substring(3,6) + "." + cpfText.substring(6,9) + "-" + cpfText.substring(9);
		}else if (cpfText.length() > 6){
			response = cpfText.substring(0,3) + "." + cpfText.substring(3,6) + "." + cpfText.substring(6);
		}else if (cpfText.length() > 3){
			response = cpfText.substring(0,3) + "." + cpfText.substring(3);
		}else{
			response = cpfText;
		}
        return (response);
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
     */
    public boolean isFinished() {
        return (cpfText.length() == CPFTEXT_LENGTH);
    }

    public int getMinLength() {
        return MASKEDCPFTEXT_LENGTH;
    }

    public int getMaxLength() {
        return MASKEDCPFTEXT_LENGTH;
    }
}