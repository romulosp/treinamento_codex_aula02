package br.gov.caixa.sispl.infra.ui.document;

public class EFLMaskCelularDocument implements EFLDocument {

	private String text;
	private int cursorPosition;

	private static final int FIELDSIZE_TELEFONE = 10;

	/**
	 *
	 */
	public EFLMaskCelularDocument() {
		text = "";
		cursorPosition = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang
	 * .String)
	 */
	public void insertTextAtCursor(String keyText) {
		if ((keyText.length() == 1) && (text.length() < FIELDSIZE_TELEFONE)) {
			char numberChar = keyText.charAt(0);
			// 99470-8991
			if (cursorPosition == 5) {
				text = text + "-";
				setCursorPosition(cursorPosition + keyText.length());
			}
			if (cursorPosition == 0) {
				if ((numberChar >= '5') && (numberChar <= '9')) {
					text = text.substring(0, cursorPosition) + keyText + text.substring(cursorPosition, text.length());
					setCursorPosition(cursorPosition + keyText.length());
				}
			} else if ((numberChar >= '0') && (numberChar <= '9')) {
				text = text.substring(0, cursorPosition) + keyText + text.substring(cursorPosition, text.length());
				setCursorPosition(cursorPosition + keyText.length());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
	 */
	public void deleteCharBeforeCursor() {
		if (cursorPosition > 0) {
			if (cursorPosition == 5) {
				text = text.replace("-", "");
			}
			text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition, text.length());
			setCursorPosition(cursorPosition - 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
	 */
	public int getCursorPosition() {
		return cursorPosition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setCursorPosition(int)
	 */
	public void setCursorPosition(int cursorPosition) {
		if (cursorPosition > text.length()) {
			this.cursorPosition = text.length();
		} else if (cursorPosition >= 0) {
			this.cursorPosition = cursorPosition;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * br.gov.caixa.sispl.infra.ui.document.EFLDocument#setText(java.lang.String)
	 */
	public void setText(String text) {
		if (text.length() > FIELDSIZE_TELEFONE) {
			this.text = text.substring(0, FIELDSIZE_TELEFONE);
		} else {
			this.text = text;
		}
		setCursorPosition(cursorPosition);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
	 */
	public String getText() {
		return text;
	}

	public String getMaskedText() {
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
	 */
	public boolean isFinished() {
		return cursorPosition == FIELDSIZE_TELEFONE;
	}

	public int getMinLength() {
		return FIELDSIZE_TELEFONE;
	}

	public int getMaxLength() {
		return FIELDSIZE_TELEFONE;
	}
}
