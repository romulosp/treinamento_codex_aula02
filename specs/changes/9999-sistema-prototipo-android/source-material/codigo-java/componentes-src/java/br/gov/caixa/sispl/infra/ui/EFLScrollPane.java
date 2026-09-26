/*
 * Created on 11/04/2005 To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 * Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;

import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkin;

/**
 * @author p504116 To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class EFLScrollPane extends JScrollPane {

    private ScrollBarSkin component;

    /**
     * @param scrollBarSkin
     * @param view
     * @param vsbPolicy
     * @param hsbPolicy
     */
    public EFLScrollPane(String scrollBarSkin, Component view, int vsbPolicy, int hsbPolicy) {
        super();
        setAutoscrolls(true);
        if (scrollBarSkin != null)
            component = (ScrollBarSkin) SkinRender.renderComponent(ComponentSkin.SCROLL_BAR, scrollBarSkin);

        setLayout(new ScrollPaneLayout.UIResource());
        setVerticalScrollBarPolicy(vsbPolicy);
        setHorizontalScrollBarPolicy(hsbPolicy);
        setViewport(createViewport());

        setVerticalScrollBar(createVerticalScrollBar());
        setHorizontalScrollBar(createHorizontalScrollBar());
        if (view != null) {
            setViewportView(view);
        }
        setOpaque(true);
        updateUI();

        if (!this.getComponentOrientation().isLeftToRight()) {
            viewport.setViewPosition(new Point(Integer.MAX_VALUE, 0));
        }
    }

    /**
     * @param scrollBarSkin
     * @param view
     */
    public EFLScrollPane(String scrollBarSkin, Component view) {
        this(scrollBarSkin, view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * @param scrollBarSkin
     * @param vsbPolicy
     * @param hsbPolicy
     */
    public EFLScrollPane(String scrollBarSkin, int vsbPolicy, int hsbPolicy) {
        this(scrollBarSkin, null, vsbPolicy, hsbPolicy);
    }

    /**
     * @param scrollBarSkin
     */
    public EFLScrollPane(String scrollBarSkin) {
        this(scrollBarSkin, null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * @param view
     * @param vsbPolicy
     * @param hsbPolicy
     */
    public EFLScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        this(null, view, vsbPolicy, hsbPolicy);
    }

    /**
     * @param view
     */
    public EFLScrollPane(Component view) {
        this(null, view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    /**
     * @param vsbPolicy
     * @param hsbPolicy
     */
    public EFLScrollPane(int vsbPolicy, int hsbPolicy) {
        this(null, null, vsbPolicy, hsbPolicy);
    }

    /**
     * 
     *
     */
    public EFLScrollPane() {
        this(null, null, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JScrollPane#createHorizontalScrollBar()
     */
    public JScrollBar createHorizontalScrollBar() {
        EFLScrollBar newHorizontalScrollBar = new EFLScrollBar(Adjustable.HORIZONTAL);
        newHorizontalScrollBar.setUnitIncrement(10);
        newHorizontalScrollBar.setBlockIncrement(100);
        return newHorizontalScrollBar;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.JScrollPane#createVerticalScrollBar()
     */
    public JScrollBar createVerticalScrollBar() {
        EFLScrollBar newVerticalScrollBar = new EFLScrollBar(Adjustable.VERTICAL);
        newVerticalScrollBar.setUnitIncrement(10);
        newVerticalScrollBar.setBlockIncrement(100);
        return newVerticalScrollBar;
    }

    protected class EFLScrollBar extends ScrollBar {

        /**
         * @param orientation
         */
        public EFLScrollBar(int orientation) {
            super(orientation);

            this.setPreferredSize(new Dimension(32, 32));

            if (component != null)
                setUI(new EFLScrollBarUI(component));
        }
    }
}
