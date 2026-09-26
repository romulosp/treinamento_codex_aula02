/*
 * Created on 12/04/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import javax.swing.plaf.basic.BasicScrollBarUI;

import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkin;

/**
 * @author p504116
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLScrollBarUI extends BasicScrollBarUI {

    private ScrollBarSkin component;

    /**
     * 
     */
    public EFLScrollBarUI(ScrollBarSkin component) {
        super();
        this.component = component;
    }

//	/*
//	 * (non-Javadoc)
//	 *
//	 * @see javax.swing.plaf.basic.BasicScrollBarUI#createDecreaseButton(int)
//	 */
//	protected JButton createDecreaseButton(int orientation) {
//		EFLButton button = null;
//		if (orientation == NORTH)
//			 button = new EFLButton(component.getRollUpButton());
//		else if(orientation==WEST)
//			button = new EFLButton(component.getRollLeftButton());
//
//		button.setPreferredSize(new Dimension(32,32));
//		return button;
//	}


//	/*
//	 * (non-Javadoc)
//	 *
//	 * @see javax.swing.plaf.basic.BasicScrollBarUI#createIncreaseButton(int)
//	 */
//	protected JButton createIncreaseButton(int orientation) {
//		EFLButton button = null;
//
//		if (orientation == SOUTH )
//			 button = new EFLButton(component.getRollDownButton());
//		else if (orientation ==EAST)
//			button = new EFLButton(component.getRollRigthButton());
//
//		button.setPreferredSize(new Dimension(32, 32));
//		return button;
//	}



}
