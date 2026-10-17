/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.togglebutton;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkinXML;
import br.gov.caixa.sispl.infra.skin.component.state.SkinStateXML;

/**
 * @author p504116
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ToggleButtonSkinXML extends ComponentSkinXML {

    private SkinStateXML disabled;
    private SkinStateXML touched;

    /**
     * @param states
     */
    public ToggleButtonSkinXML() {
        //Define o nome do componente
        setName(ComponentSkin.TOGGLE_BUTTON);
    }

    /**
     * @return
     */
    public SkinStateXML getDisabled() {
        return disabled;
    }

    /**
     * @return
     */
    public SkinStateXML getTouched() {
        return touched;
    }

    /**
     * @param stateXML
     */
    public void setDisabled(SkinStateXML stateXML) {
        disabled = stateXML;
    }

    /**
     * @param stateXML
     */
    public void setTouched(SkinStateXML stateXML) {
        touched = stateXML;
    }

}
