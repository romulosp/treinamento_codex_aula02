/*
 * Created on 25/05/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.ui.document;


/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFLUserDocument implements EFLDocument {

    private String text;
    private int cursorPosition;
    private static final int FIELDSIZE_USER = 7;

    /**
     *
     */
    public EFLUserDocument() {
        text = "";
        cursorPosition = 0;
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
     */
    public void insertTextAtCursor(String keyText) {
        if ((keyText.length() == 1) && (text.length() < FIELDSIZE_USER)) {
            char keyChar = keyText.toUpperCase().charAt(0);
            if (cursorPosition == 0) {
                if (keyChar == 'L') {
                    text = text.substring(0, cursorPosition) + keyChar + text.substring(cursorPosition,text.length());
                    setCursorPosition(cursorPosition + keyText.length());
                }
            } else if ((keyChar >= '0') && (keyChar <= '9')) {
                text = text.substring(0, cursorPosition) + keyChar + text.substring(cursorPosition,text.length());
                setCursorPosition(cursorPosition + keyText.length());
            }
        }
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
     */
    public void deleteCharBeforeCursor() {
        if (((cursorPosition == 1) && (text.length() == 1)) || (cursorPosition > 1)) {
            text = text.substring(0, cursorPosition - 1) + text.substring(cursorPosition, text.length());
            setCursorPosition(cursorPosition - 1);
        }
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
     */
    public int getCursorPosition() {
        return cursorPosition;
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setCursorPosition(int)
     */
    public void setCursorPosition(int cursorPosition) {
        if (cursorPosition > text.length()) {
            this.cursorPosition = text.length();
        } else if (cursorPosition >= 0) {
            this.cursorPosition = cursorPosition;
        }
    }


    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setText(java.lang.String)
     */
    public void setText(String text) {
        if (text.length() > FIELDSIZE_USER) {
            this.text = text.substring(0, FIELDSIZE_USER);
        } else {
            this.text = text;
        }
        setCursorPosition(cursorPosition);
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
     */
    public String getText() {
        return text;
    }

    public String getMaskedText() {
        return text;
    }


    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
     */
    public boolean isFinished() {
        return (cursorPosition == FIELDSIZE_USER);
    }

    public int getMinLength() {
        return FIELDSIZE_USER;
    }

    public int getMaxLength() {
        return FIELDSIZE_USER;
    }
}
