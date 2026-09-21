/*
 * Created on 20/04/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.component.table;

import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;

/**
 * @author Achilles e Clarissa
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TableSkin extends ComponentSkin {

    private SkinState touchedState;
    private SkinState disabledState;

    private TableHeaderSkin tableHeaderSkin;

    public static final String TABLE_EFL = "typeTable";
    public static final String TABLE_EFL_2 = "typeTable2";

    /**
     * @return
     */
    public SkinState getTouchedState() {
        return touchedState;
    }

    /**
     * @param state
     */
    public void setTouchedState(SkinState state) {
        touchedState = state;
    }

    /**
     * @return
     */
    public SkinState getDisabledState() {
        return disabledState;
    }

    /**
     * @param state
     */
    public void setDisabledState(SkinState state) {
        disabledState = state;
    }

    /**
     * @return
     */
    public TableHeaderSkin getTableHeaderSkin() {
        return tableHeaderSkin;
    }

    /**
     * @param skin
     */
    public void setTableHeaderSkin(TableHeaderSkin skin) {
        tableHeaderSkin = skin;
    }

    /**
     * 
     */




}
