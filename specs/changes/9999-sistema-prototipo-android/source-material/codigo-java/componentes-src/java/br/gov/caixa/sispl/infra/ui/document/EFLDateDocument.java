/*
 * Created on 25/05/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.ui.document;

import java.text.ParseException;
import java.text.SimpleDateFormat;


/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFLDateDocument implements EFLDocument {

    private static final int DATETEXT_LENGTH = 8;
    private static final int MASKEDDATETEXT_LENGTH = 10;
    private String dateText;


    /**
     *
     */
    public EFLDateDocument() {
        dateText = "";
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#insertTextAtCursor(java.lang.String)
     */
    public void insertTextAtCursor(String keyText) {
        if (keyText.length() == 1) {
            char numberChar = keyText.charAt(0);
            switch (dateText.length()) {
            case 0:
                if ((numberChar >= '0') && (numberChar <= '3')) {
                    dateText += numberChar;
                } else if ((numberChar >= '4') && (numberChar <= '9')) {
                    dateText += "0" + numberChar;
                }
                break;
            case 1:
                if (!((dateText.charAt(0) == '0') && (numberChar == '0'))) {
                    if (dateText.charAt(0) == '3') {
                        if ((numberChar >= '0') && (numberChar <= '1')) {
                            dateText += numberChar;
                        }
                    } else if ((numberChar >= '0') && (numberChar <= '9')) {
                        dateText += numberChar;
                    }
                }
                break;
            case 2:
                if ((numberChar >= '0') && (numberChar <= '1')) {
                    dateText += numberChar;
                } else if ((numberChar >= '2') && (numberChar <= '9')) {
                    dateText += "0" + numberChar;
                }
                break;
            case 3:
                if (!((dateText.charAt(2) == '0') && (numberChar == '0'))) {
                    if (dateText.charAt(2) == '1') {
                        if ((numberChar >= '0') && (numberChar <= '2')) {
                            dateText += numberChar;
                        }
                    } else if ((numberChar >= '0') && (numberChar <= '9')) {
                        dateText += numberChar;
                    }
                }
                break;
            case 4:
            	if ((numberChar >= '0') && (numberChar <= '9')) {
            		if ((!dateText.substring(0,4).equals("3002"))&&
            			(!dateText.substring(0,4).equals("3102"))&&
            			(!dateText.substring(0,4).equals("3104"))&&
            			(!dateText.substring(0,4).equals("3106"))&&
            			(!dateText.substring(0,4).equals("3109"))&&
            			(!dateText.substring(0,4).equals("3111"))) {
            			dateText += numberChar;
            		}
            	}
            	break;
            case 5:
            case 6:
                if ((numberChar >= '0') && (numberChar <= '9')) {
                    dateText += numberChar;
                }
                break;
            case 7:
                if ((numberChar >= '0') && (numberChar <= '9')) {
                    if (dateText.substring(0,4).equals("2902")) {
                        int yearAttempted = Integer.parseInt(dateText.substring(4,7) + numberChar);
                        if (((yearAttempted % 4) == 0) && (((yearAttempted %100) != 0) || ((yearAttempted %1000) == 0)) ) {
                            dateText += numberChar;
                        }
                    } else {
                        dateText += numberChar;
                    }
                }
                break;
            }
        }
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
     */
    public void deleteCharBeforeCursor() {
        if (dateText.length() > 0) {
            dateText = dateText.substring(0, dateText.length() - 1);
        }
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
     */
    public int getCursorPosition() {
        int cursorPosition = dateText.length();
        cursorPosition += ((cursorPosition > 1) ? 1 : 0) + ((cursorPosition > 3) ? 1 : 0);
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
        SimpleDateFormat simple = new SimpleDateFormat("ddMMyyyy");
        try {
            simple.parse(text);
            this.dateText = text;
        } catch (ParseException e) {
            //Não faz nada
        }
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
     */
    public String getText() {
        return dateText;
    }

    public String getMaskedText() {
        String response;
        if (dateText.length() > 3) {
            response = dateText.substring(0,2) + "/" + dateText.substring(2,4) + "/" + dateText.substring(4);
        } else if (dateText.length() > 1) {
            response = dateText.substring(0,2) + "/" + dateText.substring(2);
        } else {
            response = dateText;
        }
        return (response + "  /  /    ".substring(response.length()));
    }

    /* (non-Javadoc)
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#isFinished()
     */
    public boolean isFinished() {
        return (dateText.length() == DATETEXT_LENGTH);
    }

    public int getMinLength() {
        return MASKEDDATETEXT_LENGTH;
    }

    public int getMaxLength() {
        return MASKEDDATETEXT_LENGTH;
    }
}
