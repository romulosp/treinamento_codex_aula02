package br.gov.caixa.sispl.infra.ui.pnl;

import br.gov.caixa.sispl.infra.perifericos.event.KeyboardEvent;
import br.gov.caixa.sispl.infra.perifericos.event.KeyboardListener;
import br.gov.caixa.sispl.infra.perifericos.servicos.keyboard.IDeviceKeyboard;
import br.gov.caixa.sispl.infra.ui.CapturaOTPListener;
import br.gov.caixa.sispl.util.MensagemErro;

/**
 * Panel informando que é para digitar a OTP
 * 
 * @author r688989 - Natã Alves Evangelista
 * 
 */
public class PnlCapturaOTP extends PnlMensagem implements KeyboardListener {

	private static final long serialVersionUID = 1L;

	private boolean cancelado = false;

	private CapturaOTPListener listener;
	
    public PnlCapturaOTP(CapturaOTPListener listener, String titulo) {
    	// TODO [ulisses] solicitar q equipe de req crie essa msg
    	super(titulo, MensagemErro.getString("GRLI0029"));
    	this.listener = listener;
        getBtnConfirmar().setEnabled(false);
        getBtnCancelar().setEnabled(true);
	}

	protected void cancela() {
    	cancelado = true;
        getBtnCancelar().setEnabled(false);
        listener.cancelaLeituraOTP();
    }

    protected String getMensagem() {
        return MensagemErro.getString("PBSI0021");
    }
    
    /**
     * Aguarda informação da OTP pelo Cliente
     * 
     * @param keyboardEvent
     */
    public void pinTyped(KeyboardEvent keyboardEvent) {
        if (cancelado) { 
        	// botao cancelar pressionado antes do evento do pin
            return;
        }

        getBtnCancelar().setEnabled(false);

        if (keyboardEvent.getStatus() == KeyboardEvent.TIMEOUT) {
            listener.timeout();
        } 
        
        if (keyboardEvent.getStatus() == KeyboardEvent.PASSWORD_READ) {
            listener.trataOTP(keyboardEvent.getText());
        } 
        
        if (keyboardEvent.getStatus() == KeyboardEvent.PASSWORD_CANCEL ||
                   (keyboardEvent.getStatus() == KeyboardEvent.KEY_READ && keyboardEvent.getKey() == IDeviceKeyboard.KEY_CANCEL)) {
            listener.cancelaLeituraOTP();
        }
    }
}
