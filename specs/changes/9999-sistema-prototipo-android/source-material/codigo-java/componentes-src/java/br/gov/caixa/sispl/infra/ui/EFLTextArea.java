/*
 * Created on 15/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.text.Document;

import br.gov.caixa.sispl.infra.skin.SkinPainter;
import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.textarea.TextAreaSkin;

/**
 *
 * @author p504116
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLTextArea extends JTextArea implements SkinPainter {

    private TextAreaSkin component;
    private Cursor customCursor;

    /**
     * 
     * 
     */
    public EFLTextArea() {
        this(null, null, null, 0, 0);
    }

    /**
     * 
     * @param textAreaSkin
     */
    public EFLTextArea(String textAreaSkin) {
        this(textAreaSkin, null, null, 0, 0);
    }

    /**
     * 
     * @param textAreaSkin
     * @param text
     */
    public EFLTextArea(String textAreaSkin, String text) {
        this(textAreaSkin, null, text, 0, 0);
    }

    /**
     * 
     * @param textAreaSkin
     * @param rows
     * @param columns
     */
    public EFLTextArea(String textAreaSkin, int rows, int columns) {
        this(textAreaSkin, null, null, rows, columns);
    }

    /**
     * 
     * @param textAreaSkin
     * @param text
     * @param rows
     * @param columns
     */
    public EFLTextArea(String textAreaSkin, String text, int rows, int columns) {
        this(textAreaSkin, null, text, rows, columns);
    }

    /**
     * 
     * @param textAreaSkin
     * @param doc
     */
    public EFLTextArea(String textAreaSkin, Document doc) {
        this(textAreaSkin, doc, null, 0, 0);
    }

    /**
     * 
     * @param textAreaSkin
     * @param doc
     * @param text
     * @param rows
     * @param columns
     */
    public EFLTextArea(String textAreaSkin, Document doc, String text,
                       int rows, int columns) {
        super(doc, text, rows, columns);

        customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                           new ImageIcon("").getImage(), new Point(0, 0),
                           "transparente");

        if (textAreaSkin != null) {
            component = (TextAreaSkin) SkinRender.renderComponent(
                            ComponentSkin.TEXT_AREA, textAreaSkin);
            setOpaque(false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    protected void paintComponent(Graphics g) {
        // Definindo a imagem
        drawSkin(g);
        // repaint();
        super.paintComponent(g);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JComponent#paintBorder(java.awt.Graphics)
     */
    protected void paintBorder(Graphics g) {
        g.setColor(new Color(0, 0, 0, 0));
        g.drawRect(0, 0, getSize().width - 1, getSize().height - 1);
        super.paintComponent(g);

    }

    /*
     * (non-Javadoc)
     * 
     * @see br.caixa.sispl.skin.CaixaSkinRender#setSkin(java.lang.String)
     */
    public void drawSkin(Graphics g) {
        SkinState state = null;

        if (hasFocus() && component.getFocusedState() != null)
            state = component.getFocusedState();
        else if (!this.isEnabled() && component.getDisabledState() != null)
            state = component.getDisabledState();
        else
            state = component.getDefaultState();

        setFont(state.getFont());
        setForeground(state.getFontColor());

        // Definindo a imagem
        if (state.getImage() == null) {
            g.setColor(state.getBackgroundColor());

            Rectangle rectangle = getVisibleRect();
            g.fillRect(rectangle.x, rectangle.y, rectangle.width,
                       rectangle.height);
        } else
            g.drawImage(state.getImage(), 0, 0, getWidth(), getHeight(), this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (d.height == 0 || d.width == 0)
            return new Dimension(150, 150);
        return d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        if (component != null)
            super.paint(g);
        else {
            super.paintComponent(g);
            super.paintBorder(g);
            super.paintChildren(g);
        }

    }

    public Cursor getCursor() {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            return customCursor;
        }
        return super.getCursor();

    }

    public void setCursor(Cursor cursor) {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            cursor = customCursor;
        }
        super.setCursor(cursor);


    }

}
