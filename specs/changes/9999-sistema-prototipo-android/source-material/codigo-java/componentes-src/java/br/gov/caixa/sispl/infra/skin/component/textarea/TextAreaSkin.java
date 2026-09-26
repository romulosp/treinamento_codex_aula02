/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.textarea;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TextAreaSkin extends ComponentSkin {

    public static final String TYPE1 = "type1";


    private SkinState disabledState;
    private SkinState focusedState;



    /**
     * @return
     */
    public SkinState getDisabledState() {
        return disabledState;
    }

    /**
     * @return
     */
    public SkinState getFocusedState() {
        return focusedState;
    }

    /**
     * @param stateXML
     */
    public void setDisabledState(SkinState stateXML) {
        disabledState = stateXML;
    }

    /**
     * @param stateXML
     */
    public void setFocusedState(SkinState stateXML) {
        focusedState = stateXML;
    }

}
