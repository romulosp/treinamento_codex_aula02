/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.builder;

import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkinXML;
import br.gov.caixa.sispl.infra.skin.component.dialog.DialogSkin;
import br.gov.caixa.sispl.infra.skin.component.dialog.DialogSkinXML;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkinXML;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkinXML;
import br.gov.caixa.sispl.infra.skin.component.passwordfield.PasswordFieldSkin;
import br.gov.caixa.sispl.infra.skin.component.passwordfield.PasswordFieldSkinXML;
import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkin;
import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkinXML;
import br.gov.caixa.sispl.infra.skin.component.state.SkinStateXML;
import br.gov.caixa.sispl.infra.skin.component.table.TableHeaderSkin;
import br.gov.caixa.sispl.infra.skin.component.table.TableHeaderSkinXML;
import br.gov.caixa.sispl.infra.skin.component.table.TableSkin;
import br.gov.caixa.sispl.infra.skin.component.table.TableSkinXML;
import br.gov.caixa.sispl.infra.skin.component.textarea.TextAreaSkin;
import br.gov.caixa.sispl.infra.skin.component.textarea.TextAreaSkinXML;
import br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkin;
import br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkinXML;
import br.gov.caixa.sispl.infra.skin.component.togglebutton.ToggleButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.togglebutton.ToggleButtonSkinXML;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public interface SkinBuilder {

    /**
     * 
     * @param component
     * @return
     */
    public ButtonSkin createButton(ButtonSkinXML component);

    /**
     * 
     * @param component
     * @return
     */
    public PanelSkin createPanel(PanelSkinXML component);

    /**
     * 
     * @param component
     * @return
     */
    public TextFieldSkin createTextField(TextFieldSkinXML component);

    /**
     * 
     * @param component
     * @return
     */
    public TextAreaSkin createTextArea(TextAreaSkinXML component);

    /**
     * 
     * @param component
     * @return
     */
    public ToggleButtonSkin createToggleButton(ToggleButtonSkinXML component);

    /**
     * 
     * @param component
     * @return
     */
    public LabelSkin createLabel(LabelSkinXML component);

    /**
     * 
     * @param component
     * @return
     */
    public ScrollBarSkin createScrollPane(ScrollBarSkinXML component);

    /**
     * 
     * @param component
     * @return
     */
    public PasswordFieldSkin createPasswordField(PasswordFieldSkinXML component);

    /**
     * 
     * @param skinStateXml
     * @return
     */
    public SkinState createState(SkinStateXML skinStateXml);

    /**
     * @param skinXML
     * @param skinHeaderXML
     * @return
     */
    public TableSkin createTable(TableSkinXML skinXML);
    /**
     * @param skinXML
     * @param skinHeaderXML
     * @return
     */
    public TableHeaderSkin createTableHeader(TableHeaderSkinXML skinHeaderXML);

    /**
     * @param skinXML
     * @return
     */
    public DialogSkin createDialog(DialogSkinXML skinXML);

}
