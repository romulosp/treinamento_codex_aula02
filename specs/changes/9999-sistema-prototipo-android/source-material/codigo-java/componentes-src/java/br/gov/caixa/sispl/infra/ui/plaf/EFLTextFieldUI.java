package br.gov.caixa.sispl.infra.ui.plaf;

import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.plaf.ComponentUI;

import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkin;
import br.gov.caixa.sispl.infra.ui.EFLTextField;

public class EFLTextFieldUI extends ComponentUI {

    private static final int TEXTBOX_BORDER = 5;

    //Skin do TextField
    private TextFieldSkin textFieldSkin = null;

    private int textOffset = 0;

    public EFLTextFieldUI(String skin) {
        if (skin != null) {
            textFieldSkin = (TextFieldSkin) SkinRender.renderComponent(ComponentSkin.TEXT_FIELD, skin);
        }
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#installUI(javax.swing.JComponent)
     */
    public void installUI(JComponent c) {
        c.addMouseListener(new BasicEFLTextFieldListener());
    }

    /* (non-Javadoc)
     * @see javax.swing.plaf.ComponentUI#paint(java.awt.Graphics, javax.swing.JComponent)
     */
    public void paint(Graphics g, JComponent c) {
        EFLTextField eflTextField = (EFLTextField) c;
        SkinState state;
        if (eflTextField.isEditable()) {
            state = textFieldSkin.getDefaultState();
        } else {
            state = textFieldSkin.getDisabledState();
        }

        //Definindo a imagem ou a cor de fundo
        eflTextField.setBackground(state.getBackgroundColor());

        //Desenhando um retangulo com a cor de fundo e uma borda.
        g.setColor(state.getBackgroundColor());
        g.fillRect(0, 0, eflTextField.getWidth() - 1, eflTextField.getHeight() - 1);

        g.setFont(state.getFont());
        FontMetrics metricaFont = eflTextField.getFontMetrics(g.getFont());

        String text = eflTextField.getMaskedText();
        //Desenha as linhas a serem impressas.
        int firstLineTop = (int) (eflTextField.getSize().getHeight() + (metricaFont.getAscent() - metricaFont.getDescent())) / 2;

        //Define o alinhamento
        int textPosition = 0;
        if ((metricaFont.getStringBounds(text, g).getMaxX() > eflTextField.getSize().getWidth()) || eflTextField.getHorizontalAlignment() == SwingConstants.LEFT) {
            textPosition = TEXTBOX_BORDER;
        } else if (eflTextField.getHorizontalAlignment() == SwingConstants.CENTER) {
            textPosition = (int) ((eflTextField.getSize().getWidth() - metricaFont.stringWidth(text)) / 2);
        } else if (eflTextField.getHorizontalAlignment() == SwingConstants.RIGHT) {
            textPosition = (int) (eflTextField.getSize().getWidth() - metricaFont.stringWidth(text) - TEXTBOX_BORDER);
        }

        int cursorPosition = (int) (textPosition + metricaFont.getStringBounds(text.substring(0, eflTextField.getCursorPosition()), g).getMaxX());

        //Verifica se o cursor pode ser visto na caixa de texto. Se não regulariza o cursor até o mesmo se encontrar dentro.
        while ((cursorPosition + textOffset) > (eflTextField.getSize().getWidth() - TEXTBOX_BORDER)) {
            textOffset -=(int) (eflTextField.getSize().getWidth() / 4);
        }
        while ((cursorPosition + textOffset) < TEXTBOX_BORDER) {
            textOffset +=(int) (eflTextField.getSize().getWidth() / 4);
        }
        cursorPosition += textOffset;
        textPosition += textOffset;

        //Mostra a seleção apenas se estiver em foco
        if (eflTextField.isMarked() && eflTextField.isEditable() && eflTextField.hasFocus()) {
            g.setColor(textFieldSkin.getFocusedState().getBackgroundColor());
            g.fillRect(textPosition, firstLineTop + metricaFont.getDescent() - metricaFont.getHeight(), metricaFont.stringWidth(text), metricaFont.getHeight());
            g.setColor(textFieldSkin.getFocusedState().getFontColor());
        } else {
            g.setColor(state.getFontColor());
        }

        //Desenha o texto e a borda da linha.
        g.drawString(text, textPosition, firstLineTop);
        g.setColor(state.getFontColor());
        g.drawRect(0, 0, eflTextField.getWidth() - 1, eflTextField.getHeight() - 1);

//		//Desenha o cursor.(somente se tiver o foco).
        if (eflTextField.hasFocus() && eflTextField.isEditable()) {
            g.drawLine(cursorPosition - 2, 3, cursorPosition - 1, 3);
            g.drawLine(cursorPosition + 1 , 3, cursorPosition + 2, 3);
            g.drawLine(cursorPosition, 4, cursorPosition, eflTextField.getHeight() - 5);
            g.drawLine(cursorPosition - 2, eflTextField.getHeight() - 4, cursorPosition - 1, eflTextField.getHeight() - 4);
            g.drawLine(cursorPosition + 1 , eflTextField.getHeight() - 4, cursorPosition + 2, eflTextField.getHeight() - 4);
        }
    }
}
