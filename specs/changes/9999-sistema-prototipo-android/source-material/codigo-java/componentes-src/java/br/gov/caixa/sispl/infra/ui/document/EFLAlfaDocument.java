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
public class EFLAlfaDocument implements EFLDocument {

    private String text;
    private int cursorPosition;
    private int minLength;
    private int maxLength;
    private boolean alwaysUpperCase;

    /**
     *
     */
    public EFLAlfaDocument() {
        text = "";
        cursorPosition = 0;
        minLength = 0;
        maxLength = 0;
        alwaysUpperCase = false;
    }

    /**
     * @param length
     */
    public EFLAlfaDocument(int fixedLength) {
        this();
        this.minLength = fixedLength;
        this.maxLength = fixedLength;
    }

    /**
     * @param length
     */
    public EFLAlfaDocument(int fixedLength, boolean alwaysUppercase) {
        this();
        this.minLength = fixedLength;
        this.maxLength = fixedLength;
        this.alwaysUpperCase = alwaysUppercase;
    }

    /**
     * @param length
     */
    public EFLAlfaDocument(int minLength, int maxLength) {
        this();
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
     */
    public void insertTextAtCursor(String keyText) {
    	if ((keyText.length() == 1) && ((maxLength == 0) || (text.length() < maxLength))) {
    		char textChar = keyText.charAt(0);
            if (validaCaracter(String.valueOf(textChar))){

            	text = text.substring(0, cursorPosition) + (alwaysUpperCase ? keyText.toUpperCase() : keyText) + text.substring(cursorPosition,text.length());
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
        if ((maxLength != 0) && (text.length() > maxLength)) {
            this.text = text.substring(0, maxLength);
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
        return  (maxLength != 0) && (cursorPosition == maxLength);
    }

    public int getMinLength() {
        return minLength;
    }

    public int getMaxLength() {
        return maxLength;
    }

    /**
     * @return Returns the alwaysUpperCase.
     */
    public boolean isAlwaysUpperCase() {
        return alwaysUpperCase;
    }

    /**
     * @param alwaysUpperCase The alwaysUpperCase to set.
     */
    public void setAlwaysUpperCase(boolean alwaysUpperCase) {
        this.alwaysUpperCase = alwaysUpperCase;
    }

    private boolean validaCaracter(String texto){
    	String validos = "abcdefghijklmnopqrstuvxwyzABCDEFGHIJKLMNOPQRSTUVXWYZ ";
    	if (validos.indexOf(texto) != -1){
    		return true;
    	}
    	return false;
    }
}
