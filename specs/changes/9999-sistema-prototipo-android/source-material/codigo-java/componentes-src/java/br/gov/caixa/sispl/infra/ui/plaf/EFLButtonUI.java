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
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.util.SkinUtil;
import br.gov.caixa.sispl.infra.ui.EFLButton;

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFLButtonUI extends ComponentUI {

    private ButtonSkin buttonSkin = null;

    public EFLButtonUI(String skin) {
        if (skin != null) {
            buttonSkin = (ButtonSkin) SkinRender.renderComponent(ComponentSkin.BUTTON, skin);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
     */
    public void installUI(JComponent c) {
        c.addMouseListener(new BasicEFLButtonListener());
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics, javax.swing.JComponent)
     */
    public void paint(Graphics g, JComponent c) {

        EFLButton eflButton = (EFLButton) c;
        SkinState state = null;

        //Definindo o estado a ser piintado
        if (eflButton.isArmed() && eflButton.isPressed() && eflButton.isEnabled()) {
            state = buttonSkin.getTouchedState();
        } else if (!eflButton.isEnabled() && buttonSkin.getDisabledState() != null) {
            state = buttonSkin.getDisabledState();
        } else {
            state = buttonSkin.getDefaultState();
        }

        //Definindo a imagem ou a cor de fundo
        if (state.getImage() == null) {
            eflButton.setBackground(state.getBackgroundColor());
        } else {
            g.drawImage(state.getImage(), 0, 0, eflButton.getWidth(), eflButton.getHeight(), eflButton);
        }

        Dimension eflButtonDimension = eflButton.getSize();

        ImageIcon icon = eflButton.getIcon();
        if (icon != null) {
            g.drawImage(icon.getImage(), (int) (eflButtonDimension.getWidth() - icon.getIconWidth()) / 2, (int) (eflButtonDimension.getHeight() - icon.getIconHeight()) / 2,icon.getIconWidth(), icon.getIconHeight(), eflButton);
        } else {
            g.setFont(state.getFont());
            g.setColor(state.getFontColor());
            FontMetrics metricaFont = eflButton.getFontMetrics(g.getFont());

            SkinUtil.alignText(g, SwingConstants.CENTER, eflButton.getText(), state.getSpacement(), metricaFont, eflButtonDimension);
        }
    }
}
