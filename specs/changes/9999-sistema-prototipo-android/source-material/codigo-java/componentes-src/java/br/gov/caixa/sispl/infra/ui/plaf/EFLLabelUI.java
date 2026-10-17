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
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.util.SkinUtil;
import br.gov.caixa.sispl.infra.ui.EFLLabel;

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EFLLabelUI extends ComponentUI {

    private LabelSkin labelSkin = null;

    public EFLLabelUI(String skin) {
        if (skin != null) {
            labelSkin = (LabelSkin) SkinRender.renderComponent(ComponentSkin.LABEL, skin);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
     */
    public void installUI(JComponent c) {
        // TODO Auto-generated method stub
        super.installUI(c);
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics, javax.swing.JComponent)
     */
    public void paint(Graphics g, JComponent c) {
        EFLLabel eflLabel = (EFLLabel) c;
        SkinState state = labelSkin.getDefaultState();

        //Definindo a imagem
        if (state.getImage() == null) {
            Rectangle rectangle = eflLabel.getVisibleRect();
            g.setColor(state.getBackgroundColor());
            g.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);

        } else {
            g.drawImage(state.getImage(), 0, 0, eflLabel.getWidth(), eflLabel.getHeight(), eflLabel);
        }

        Dimension eflLabelDimension = eflLabel.getSize();


        ImageIcon icon = eflLabel.getIcon();
        if (icon != null) {
            //g.drawImage(icon.getImage(), (int) (eflLabelDimension.getWidth() - icon.getIconWidth()) / 2, (int) (eflLabelDimension.getHeight() - icon.getIconHeight()) / 2,icon.getIconWidth(), icon.getIconHeight(), eflLabel);
        } else {
            g.setFont(state.getFont());
            g.setColor(state.getFontColor());
            //Capturando a fonte como imagem
            FontMetrics metricaFont = eflLabel.getFontMetrics(g.getFont());
            SkinUtil.alignText(g, eflLabel.getHorizontalAlignment(), eflLabel.getText(), state.getSpacement(), metricaFont, eflLabelDimension);
        }

    }
}
