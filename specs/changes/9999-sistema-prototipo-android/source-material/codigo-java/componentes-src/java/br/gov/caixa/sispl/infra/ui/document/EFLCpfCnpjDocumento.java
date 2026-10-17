package br.gov.caixa.sispl.infra.ui.document;

public class EFLCpfCnpjDocumento implements EFLDocument {

	private static final int CPFTEXT_LENGTH = 14;
	private static final int MASKEDCPFTEXT_LENGTH = 18;
	private String mask;
	private int cursorPosition;
	private int maxLength;

	/**
	 * f552809 - 21-11-2023
	 */
	public EFLCpfCnpjDocumento() {
		mask = "";
		maxLength = 18;
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
	 */
	public void insertTextAtCursor(String keyText) {
		if ((keyText.length() == 1) && (getMaskedText().length() < getMaxLength())) {
			char numberChar = keyText.charAt(0);
			if ((numberChar >= '0') && (numberChar <= '9')) {
				mask += numberChar;
			}
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
	 */
	public void deleteCharBeforeCursor() {
		if (mask.length() > 0) {
			mask = mask.substring(0, mask.length() - 1);
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
	 */
	public int getCursorPosition() {
		cursorPosition = mask.length();
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
			this.mask = text.substring(0, maxLength);
		} else {
			this.mask = text;
		}
	}

	/* (non-Javadoc)
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
	 */
	public String getText() {
		return mask;
	}

	public String getMaskedText() {
		String response;
		
		//000.000.000-00
		if(mask.length() <= 11) {
			if (mask.length() > 9) {
				response = mask.substring(0,3) + "." + mask.substring(3,6) + "." + mask.substring(6,9) + "-" + mask.substring(9);
			}else if (mask.length() > 6){
				response = mask.substring(0,3) + "." + mask.substring(3,6) + "." + mask.substring(6);
			}else if (mask.length() > 3){
				response = mask.substring(0,3) + "." + mask.substring(3);
			}else{
				response = mask;
			}
		}else {
			//00.000.000/0000-00
			if (mask.length() > 12) {
				response = mask.substring(0,2) + "." + mask.substring(2,5) + "." + mask.substring(5,8) + "/" + mask.substring(8,12) + "-" + mask.substring(12);
			}else if (mask.length() > 9) {
				response = mask.substring(0,2) + "." + mask.substring(2,5) + "." + mask.substring(5,8) + "/" + mask.substring(8);
			}else if (mask.length() > 5){
				response = mask.substring(0,2) + "." + mask.substring(2,5) + "." + mask.substring(5);
			}else if (mask.length() > 2){
				response = mask.substring(0,2) + "." + mask.substring(2);
			}else{
				response = mask;
			}
		}
		
        return (response);
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
     */
    public boolean isFinished() {
        return (mask.length() == CPFTEXT_LENGTH) ;
    }

    public int getMinLength() {
        return MASKEDCPFTEXT_LENGTH;
    }

    public int getMaxLength() {
        return MASKEDCPFTEXT_LENGTH;
    }
}