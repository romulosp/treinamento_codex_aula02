/*
 * Created on 20/04/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.table;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkinXML;
import br.gov.caixa.sispl.infra.skin.component.state.SkinStateXML;

/**
 * @author Achilles e Clarissa
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TableSkinXML extends ComponentSkinXML {

    private SkinStateXML touched;
    private SkinStateXML disabled;

    private TableHeaderSkinXML tableHeaderSkinXml;

    public TableSkinXML() {
        //Define o nome do componente
        setName(ComponentSkin.TABLE);
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
    public void setTouched(SkinStateXML stateXML) {
        touched = stateXML;
    }

    /**
     * @return
     */
    public SkinStateXML getDisabled() {
        return disabled;
    }

    /**
     * @param stateXML
     */
    public void setDisabled(SkinStateXML stateXML) {
        disabled = stateXML;
    }

    /**
     * @return
     */
    public TableHeaderSkinXML getTableHeaderSkinXml() {
        return tableHeaderSkinXml;
    }

    /**
     * @param skinXML
     */
    public void setTableHeaderSkinXml(TableHeaderSkinXML skinXML) {
        tableHeaderSkinXml = skinXML;
    }

}
