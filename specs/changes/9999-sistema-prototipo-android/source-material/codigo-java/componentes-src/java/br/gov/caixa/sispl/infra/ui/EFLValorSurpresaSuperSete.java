/*
 * Created on 15/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import javax.swing.SwingConstants;

public class EFLValorSurpresaSuperSete extends EFLLabel implements SwingConstants {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1561585015997760443L;
	private EFLToggleButton botao;
	
    /**
     * Construtor que recebe apenas o tipo de skin do label
     * @param labelSkin String skin do label
     */
    public EFLValorSurpresaSuperSete(String labelSkin) {
        super(labelSkin, null, RIGHT);
    }

    /**
     * Construtor que recebe um tipo de skin para o label, e um texto 
     * @param labelSkin String tipo de skin desse componente
     * @param text String text do label
     */
    public EFLValorSurpresaSuperSete(String labelSkin, String text) {
        super(labelSkin, text, RIGHT);
    }
    
    public EFLValorSurpresaSuperSete(String labelSkin, String text, EFLToggleButton botao) {
        super(labelSkin, text, RIGHT);
        this.add(botao);
        this.botao = botao;
    }

	public EFLToggleButton getBotao() {
		return botao;
	}

	public void setBotao(EFLToggleButton botao) {
		this.botao = botao;
	}


}
