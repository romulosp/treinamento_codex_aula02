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
public class EFLNsbDocument implements EFLDocument {

    protected String text;
    protected int cursorPosition;
    protected static final int NSB_LENGTH = 23;

    /**
     *
     */
    public EFLNsbDocument() {
        text = "";
        cursorPosition = 0;
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
     */
    public void insertTextAtCursor(String keyText) {
        if ((keyText.length() == 1) && (text.length() < NSB_LENGTH)) {
            char nsbChar = keyText.toUpperCase().charAt(0);
            if (((nsbChar >= '0') && (nsbChar <= '9')) || ((nsbChar >= 'A') && (nsbChar <= 'F'))) {
                text = text.substring(0, cursorPosition) + keyText.toUpperCase() + text.substring(cursorPosition,text.length());
                setCursorPosition(cursorPosition + keyText.length());
            }
        }
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
     */
    public void deleteCharBeforeCursor() {
        if (cursorPosition > 0) {
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
        if ((NSB_LENGTH != 0) && (text.length() > NSB_LENGTH)) {
            this.text = text.substring(0, NSB_LENGTH);
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
        return (cursorPosition == NSB_LENGTH);
    }

    public int getMinLength() {
        return NSB_LENGTH;
    }

    public int getMaxLength() {
        // TODO Auto-generated method stub
        return NSB_LENGTH;
    }
}
