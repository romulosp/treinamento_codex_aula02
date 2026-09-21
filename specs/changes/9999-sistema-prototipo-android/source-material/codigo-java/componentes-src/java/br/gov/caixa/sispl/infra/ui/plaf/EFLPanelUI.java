/*
 * Created on 03/08/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.ui.plaf;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.EFLPanel;

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFLPanelUI extends ComponentUI {

    private PanelSkin panelSkin = null;

    public EFLPanelUI(String skin) {
        if (skin != null) {
            panelSkin = (PanelSkin) SkinRender.renderComponent(ComponentSkin.PANEL, skin);
        }
    }

    public void installUI(JComponent c) {
        c.addKeyListener(new BasicEFLPanelListener());
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics, javax.swing.JComponent)
     */
    public void paint(Graphics g, JComponent c) {
        EFLPanel eflPanel = (EFLPanel) c;
        SkinState state = panelSkin.getDefaultState();

        //Definindo a imagem ou a cor de fundo
        if (state.getImage() == null) {
            eflPanel.setBackground(state.getBackgroundColor());
            g.setColor(state.getBackgroundColor());
            g.fillRect(0, 0, eflPanel.getWidth() - 1, eflPanel.getHeight() - 1);
            g.setColor(state.getFontColor());
            g.drawRect(0, 0, eflPanel.getWidth() - 1, eflPanel.getHeight() - 1);
        } else {
            g.drawImage(state.getImage(), 0, 0, eflPanel.getWidth(), eflPanel.getHeight(), eflPanel);
        }
    }
}
