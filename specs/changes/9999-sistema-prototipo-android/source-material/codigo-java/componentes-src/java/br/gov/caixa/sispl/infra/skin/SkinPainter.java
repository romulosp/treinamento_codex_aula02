/*
 * Created on 16/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin;

import java.awt.Graphics;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface SkinPainter {

    /**
     * Draw the component skin
     * @param graphic
     */
    public abstract void drawSkin(Graphics graphic);

}
