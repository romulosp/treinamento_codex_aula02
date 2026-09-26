/*
 * Created on 15/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.KeyboardFocusManager;

import javax.swing.JComponent;

import br.gov.caixa.sispl.infra.ui.plaf.EFLPanelUI;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLPanel extends JComponent {

	private static final long serialVersionUID = 2007121201L;

	protected UIManager uiManager;

    public EFLPanel() {
        uiManager = new DefaultUIManager();
        //Usado para que a Tecla TAB seja reconhecida.
        this.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, java.util.Collections.EMPTY_SET);
    }

    /**
     * 
     * @param panelSkin
     */
    public EFLPanel(String panelSkin) {
        this();
        setUI(new EFLPanelUI(panelSkin));
    }

    /**
     * @return Returns the uiManager.
     */
    public UIManager getUiManager() {
        return uiManager;
    }
    /**
     * @param uiManager The uiManager to set.
     */
    public void setUiManager(UIManager uiManager) {
        this.uiManager = uiManager;
    }
}
