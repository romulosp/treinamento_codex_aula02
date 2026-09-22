package br.gov.caixa.sispl.infra.ui.document;

import br.gov.caixa.sispl.infra.util.NsbUtil;

/**
 * Classe criada para utilização no caso de uso: Controla obtencao DV de NSB - EFL

 * @author r688989 - Natã Alves Evangelista
 *
 */
public class EFLNsbMascaradoDocument extends EFLNsbDocument {

	private static final int MASKEDNSBTEXT_LENGTH = 27;
	
	public void insertTextAtCursor(String keyText) {
		int qtdChar = NsbUtil.isAntigo(text) ? 21 : 22;
		char nsbChar = keyText.toUpperCase().charAt(0);
        if ((keyText.length() == 1) && (text.length() < qtdChar)) {
            if (((nsbChar >= '0') && (nsbChar <= '9')) || ((nsbChar >= 'A') && (nsbChar <= 'F'))) {
            	atribuiCaracter(keyText);
            }
        } else
        
        if((keyText.length() == 1) && (text.length() >= qtdChar)){
        	if('X' == nsbChar || 'x' == nsbChar){
        		atribuiCaracter(keyText);
        	}
        }
    }
	
	private void atribuiCaracter(String keyText){
		text = text.substring(0, cursorPosition) + keyText.toUpperCase() + text.substring(cursorPosition,text.length());
		setCursorPosition(cursorPosition + keyText.length());
	}
	
	/**
	 * Específico para a mascara do DV.
	 */
	public int getCursorPosition() {
        int cursorPosition = text.length();
        cursorPosition += ((cursorPosition > 3) ? 1 : 0) + ((NsbUtil.isAntigo(text) && cursorPosition > 22) || ((!NsbUtil.isAntigo(text) && cursorPosition > 23)) ? 1 : 0);
        return cursorPosition;
    }
	
	public void setText(String text) {
        if ((NSB_LENGTH != 0) && (text.length() > NSB_LENGTH)) {
            this.text = text.substring(0, NSB_LENGTH);
        } else {
            this.text = text;
        }
        setCursorPosition(cursorPosition);
    }
	
	/**
	 * Mascara para o campo NSB
	 * Formato: 9999-99999999999999999-XX
	 */
	public String getMaskedText() {
		String response;
        if (text.length() > 21) {
            response = text.substring(0,4) + "-" + text.substring(4, (NsbUtil.isAntigo(text) ? 21 : 22)) + "-" 
            		+ text.substring((NsbUtil.isAntigo(text) ? 21 : 22));
        } else if (text.length() > 3) {
            response = text.substring(0,4) + "-" + text.substring(4);
        } else {
            response = text;
        }
        if(response == null || response.isEmpty()) {
        	return (response + "    -                 -XX".substring(response.length()));
        }else {
        	return (response 
        			+ ("    -                 " + (NsbUtil.isAntigo(response) ? "-XX" : " -X")).substring(response.length()));
        }
    }
	
	public boolean isFinished() {
		return (text.length() == NSB_LENGTH);
	}

	public int getMinLength() {
        return MASKEDNSBTEXT_LENGTH;
    }

    public int getMaxLength() {
        return MASKEDNSBTEXT_LENGTH;
    }
}
