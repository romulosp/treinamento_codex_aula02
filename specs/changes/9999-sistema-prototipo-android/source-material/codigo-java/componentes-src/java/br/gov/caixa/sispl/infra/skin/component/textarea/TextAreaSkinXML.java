/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.textarea;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkinXML;
import br.gov.caixa.sispl.infra.skin.component.state.SkinStateXML;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TextAreaSkinXML extends ComponentSkinXML {

    private SkinStateXML disabled;
    private SkinStateXML focused;

    public TextAreaSkinXML() {
        this.setName(ComponentSkin.TEXT_AREA);
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
    public SkinStateXML getFocused() {
        return focused;
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
    public void setFocused(SkinStateXML stateXML) {
        focused = stateXML;
    }

}
