/*
 * Created on 03/08/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.caixa.sispl.infra.ui.plaf;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;

import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.togglebutton.ToggleButtonSkin;
import br.gov.caixa.sispl.infra.skin.util.SkinUtil;
import br.gov.caixa.sispl.infra.ui.EFLToggleButton;

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFLToggleButtonUI extends ComponentUI {

    private ToggleButtonSkin toggleButtonSkin = null;

    public EFLToggleButtonUI(String skin) {
        if (skin != null) {
            toggleButtonSkin = (ToggleButtonSkin) SkinRender.renderComponent(ComponentSkin.TOGGLE_BUTTON, skin);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
     */
    public void installUI(JComponent c) {
        c.addMouseListener(new BasicEFLToggleButtonListener());
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics, javax.swing.JComponent)
     */
    public void paint(Graphics g, JComponent c) {

        EFLToggleButton eflToggleButton = (EFLToggleButton) c;
        SkinState state = null;

        //Definindo o estado a ser pintado
        if (eflToggleButton.isSelected()) {
            state = toggleButtonSkin.getTouchedState();
        } else if (!eflToggleButton.isEnabled() && toggleButtonSkin.getDisabledState() != null) {
            state = toggleButtonSkin.getDisabledState();
        } else {
            state = toggleButtonSkin.getDefaultState();
        }

        //Definindo a imagem ou a cor de fundo
        if (state.getImage() == null) {
            eflToggleButton.setBackground(state.getBackgroundColor());
        } else {
            g.drawImage(state.getImage(), 0, 0, eflToggleButton.getWidth(), eflToggleButton.getHeight(), eflToggleButton);
        }

        Dimension eflButtonDimension = eflToggleButton.getSize();

        ImageIcon icon = eflToggleButton.getIcon();
        if (icon != null) {
            g.drawImage(icon.getImage(), (int) (eflButtonDimension.getWidth() - icon.getIconWidth()) / 2, (int) (eflButtonDimension.getHeight() - icon.getIconHeight()) / 2,icon.getIconWidth(), icon.getIconHeight(), eflToggleButton);
        } else {
            g.setFont(state.getFont());
            g.setColor(state.getFontColor());
            FontMetrics metricaFont = eflToggleButton.getFontMetrics(g.getFont());

            SkinUtil.alignText(g, SwingConstants.CENTER, eflToggleButton.getText(), state.getSpacement(), metricaFont, eflButtonDimension);
        }
    }
}
