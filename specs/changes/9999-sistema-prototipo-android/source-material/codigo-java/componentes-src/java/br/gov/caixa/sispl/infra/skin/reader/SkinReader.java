/*
 * Created on 14/04/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.reader;

import java.util.List;

import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkinXML;
import br.gov.caixa.sispl.infra.skin.component.dialog.DialogSkinXML;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkinXML;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkinXML;
import br.gov.caixa.sispl.infra.skin.component.passwordfield.PasswordFieldSkinXML;
import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkinXML;
import br.gov.caixa.sispl.infra.skin.component.state.SkinStateXML;
import br.gov.caixa.sispl.infra.skin.component.table.TableHeaderSkinXML;
import br.gov.caixa.sispl.infra.skin.component.table.TableSkinXML;
import br.gov.caixa.sispl.infra.skin.component.textarea.TextAreaSkinXML;
import br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkinXML;
import br.gov.caixa.sispl.infra.skin.component.togglebutton.ToggleButtonSkinXML;
import br.gov.caixa.sispl.infra.skin.exception.SkinException;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class SkinReader {

    public static final String DEFAULT_STATE = "default";
    public static final String TOUCHED_STATE = "touched";
    public static final String DISABLED_STATE = "disabled";
    public static final String FOCUSED_STATE = "focused";

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract ButtonSkinXML getButton(String type) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract PanelSkinXML getPanel(String type) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract LabelSkinXML getLabel(String type) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract ToggleButtonSkinXML getToggleButton(String type) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract TextFieldSkinXML getTextField(String type) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract TextAreaSkinXML getTextArea(String type) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract PasswordFieldSkinXML getPasswordField(String type) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract ScrollBarSkinXML getScrollPane(String type) throws SkinException;

    /**
     * 
     * @param name
     * @return
     * @throws SkinException
     */
    public abstract SkinStateXML getStateByName(List states, String name) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract TableSkinXML getTable(String type) throws SkinException;

    /**
     * 
     * @param type
     * @return
     * @throws SkinException
     */
    public abstract TableHeaderSkinXML getTableHeader(String type) throws SkinException;

    /**
     * @param string
     */
    public abstract DialogSkinXML getDialog(String string) throws SkinException;


}
