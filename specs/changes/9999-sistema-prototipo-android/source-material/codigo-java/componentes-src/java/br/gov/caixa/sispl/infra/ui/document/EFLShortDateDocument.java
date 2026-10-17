package br.gov.caixa.sispl.infra.ui.document;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Essa classe é utilizada no caso de uso Recebe Declaração de Isento, para o tratamento de datas no formato dd/MM/yy
 *
 * @author p543713
 * @version 1.0
 * @since 28/07/2006
 */
public class EFLShortDateDocument implements EFLDocument {

    private static final int DATETEXT_LENGTH = 6;
    private static final int MASKEDDATETEXT_LENGTH = 8;
    private String dateText;

    /**
     * Construtor Padrão
     */
    public EFLShortDateDocument() {
        dateText = "";
    }

    /*
     * (non-Javadoc)
     * 
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
            case 5:
                if ((numberChar >= '0') && (numberChar <= '9')) {
                    dateText += numberChar;
                }
                break;

            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#deleteCharBeforeCursor()
     */
    public void deleteCharBeforeCursor() {
        if (dateText.length() > 0) {
            dateText = dateText.substring(0, dateText.length() - 1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getCursorPosition()
     */
    public int getCursorPosition() {
        int cursorPosition = dateText.length();
        cursorPosition += ((cursorPosition > 1) ? 1 : 0) + ((cursorPosition > 3) ? 1 : 0);
        return cursorPosition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setCursorPosition(int)
     */
    public void setCursorPosition(int cursorPosition) {
        //Não faz nada.
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#setText(java.lang.String)
     */
    public void setText(String text) {
        SimpleDateFormat simple = new SimpleDateFormat("ddMMyy");
        try {
            simple.parse(text);
            this.dateText = text;
        } catch (ParseException e) {
            //Não faz nada
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.ui.document.EFLDocument#getText()
     */
    public String getText() {
        return dateText;
    }

    public String getMaskedText() {
        String response;
        if (dateText.length() > 3) {
            response = dateText.substring(0, 2) + "/" + dateText.substring(2, 4) + "/" + dateText.substring(4);
        } else if (dateText.length() > 1) {
            response = dateText.substring(0, 2) + "/" + dateText.substring(2);
        } else {
            response = dateText;
        }
        return (response + "  /  /  ".substring(response.length()));
    }

    /*
     * (non-Javadoc)
     * 
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
