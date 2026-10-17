/*
 * Created on 23/03/2005 To change the template for this generated file go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 * Comments
 */
package br.gov.caixa.sispl.infra.skin.reader.impl.jaxb;

import java.util.Iterator;
import java.util.List;

import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import br.gov.caixa.sispl.infra.skin.SkinConfig;
import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkinXML;
import br.gov.caixa.sispl.infra.skin.component.dialog.DialogSkinXML;
import br.gov.caixa.sispl.infra.skin.component.label.LabelSkinXML;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkinXML;
import br.gov.caixa.sispl.infra.skin.component.passwordfield.PasswordFieldSkinXML;
import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkinXML;
import br.gov.caixa.sispl.infra.skin.component.state.ColorBean;
import br.gov.caixa.sispl.infra.skin.component.state.FontBean;
import br.gov.caixa.sispl.infra.skin.component.state.SkinStateXML;
import br.gov.caixa.sispl.infra.skin.component.table.TableHeaderSkinXML;
import br.gov.caixa.sispl.infra.skin.component.table.TableSkinXML;
import br.gov.caixa.sispl.infra.skin.component.textarea.TextAreaSkinXML;
import br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkinXML;
import br.gov.caixa.sispl.infra.skin.component.togglebutton.ToggleButtonSkinXML;
import br.gov.caixa.sispl.infra.skin.exception.SkinException;
import br.gov.caixa.sispl.infra.skin.reader.SkinReader;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.Button;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.Dialog;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.Label;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.Panel;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.PasswordField;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.ScrollBar;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.Skin;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.State;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.Table;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.TextArea;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.TextField;
import br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml.ToggleButton;

/**
 * @author p532406 To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class JaxbSkinReader extends SkinReader {

    private static final Log log = LogFactory.getLog(JaxbSkinReader.class);

    /** Objeto root do xml */
    private Skin component;

    private static JaxbSkinReader instance;

    public static final String SKIN_XML_PACKAGE = "br.gov.caixa.sispl.infra.skin.reader.impl.jaxb.xml";

    private JaxbSkinReader() {
        try {
            Unmarshaller unmarshaller = JAXBContext.newInstance(SKIN_XML_PACKAGE, this.getClass().getClassLoader()).createUnmarshaller();

            component = (Skin) unmarshaller.unmarshal(this.getClass().getResourceAsStream(SkinConfig.getSkinXmlPath()));
        } catch (JAXBException e1) {
            log.error("Erro no Jaxb", e1);
        }
    }

    public static JaxbSkinReader getInstance() {
        if (instance == null)
            instance = new JaxbSkinReader();
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getButton()
     */
    public ButtonSkinXML getButton(String type) throws SkinException {
        for (Iterator iter = component.getButton().iterator(); iter.hasNext();) {
            Button button = (Button) iter.next();
            if (button.getName().equals(type)) {

                ButtonSkinXML buttonSkinXml = new ButtonSkinXML();
                buttonSkinXml.setDefaultState(getStateByName(button.getState(), DEFAULT_STATE));
                buttonSkinXml.setTouched(getStateByName(button.getState(), TOUCHED_STATE));
                buttonSkinXml.setDisabled(getStateByName(button.getState(), DISABLED_STATE));

                return buttonSkinXml;
            }

        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Button type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getPanel()
     */
    public PanelSkinXML getPanel(String type) throws SkinException {
        for (Iterator iter = component.getPanel().iterator(); iter.hasNext();) {
            Panel panel = (Panel) iter.next();
            if (panel.getName().equals(type)) {

                PanelSkinXML panelSkinXml = new PanelSkinXML();
                panelSkinXml.setDefaultState(toSkinStateXml(panel.getState()));

                return panelSkinXml;
            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Panel type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getLabel()
     */
    public LabelSkinXML getLabel(String type) throws SkinException {
        for (Iterator iter = component.getLabel().iterator(); iter.hasNext();) {
            Label label = (Label) iter.next();
            if (label.getName().equals(type)) {

                LabelSkinXML labelSkinXml = new LabelSkinXML();
                labelSkinXml.setDefaultState(toSkinStateXml(label.getState()));

                return labelSkinXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Label type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getToggleButton()
     */
    public ToggleButtonSkinXML getToggleButton(String type) throws SkinException {
        for (Iterator iter = component.getToggleButton().iterator(); iter.hasNext();) {
            ToggleButton button = (ToggleButton) iter.next();
            if (button.getName().equals(type)) {

                ToggleButtonSkinXML toggleButtonSkinXml = new ToggleButtonSkinXML();
                toggleButtonSkinXml.setDefaultState(getStateByName(button.getState(), DEFAULT_STATE));
                toggleButtonSkinXml.setDisabled(getStateByName(button.getState(), DISABLED_STATE));
                toggleButtonSkinXml.setTouched(getStateByName(button.getState(), TOUCHED_STATE));

                return toggleButtonSkinXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Toggle Button type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getTextField()
     */
    public TextFieldSkinXML getTextField(String type) throws SkinException {
        for (Iterator iter = component.getTextField().iterator(); iter.hasNext();) {
            TextField textField = (TextField) iter.next();
            if (textField.getName().equals(type)) {

                TextFieldSkinXML textFieldSkinXml = new TextFieldSkinXML();
                textFieldSkinXml.setDefaultState(getStateByName(textField.getState(), DEFAULT_STATE));
                textFieldSkinXml.setDisabled(getStateByName(textField.getState(), DISABLED_STATE));
                textFieldSkinXml.setFocused(getStateByName(textField.getState(), FOCUSED_STATE));

                return textFieldSkinXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Text Field type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getTextArea()
     */
    public TextAreaSkinXML getTextArea(String type) throws SkinException {
        for (Iterator iter = component.getTextArea().iterator(); iter.hasNext();) {
            TextArea textArea = (TextArea) iter.next();
            if (textArea.getName().equals(type)) {

                TextAreaSkinXML textAreaSkinXml = new TextAreaSkinXML();
                textAreaSkinXml.setDefaultState(getStateByName(textArea.getState(), DEFAULT_STATE));
                textAreaSkinXml.setDisabled(getStateByName(textArea.getState(), DISABLED_STATE));
                textAreaSkinXml.setFocused(getStateByName(textArea.getState(), FOCUSED_STATE));

                return textAreaSkinXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Text Area type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getPasswordField()
     */
    public PasswordFieldSkinXML getPasswordField(String type) throws SkinException {
        for (Iterator iter = component.getPasswordField().iterator(); iter.hasNext();) {
            PasswordField passwordTextField = (PasswordField) iter.next();
            if (passwordTextField.getName().equals(type)) {

                PasswordFieldSkinXML passwordTextFieldSkinXml = new PasswordFieldSkinXML();
                passwordTextFieldSkinXml.setDefaultState(getStateByName(passwordTextField.getState(), DEFAULT_STATE));
                passwordTextFieldSkinXml.setDisabled(getStateByName(passwordTextField.getState(), DISABLED_STATE));
                passwordTextFieldSkinXml.setFocused(getStateByName(passwordTextField.getState(), FOCUSED_STATE));

                return passwordTextFieldSkinXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Text Field type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getScrollPane()
     */
    public ScrollBarSkinXML getScrollPane(String type) throws SkinException {

        for (Iterator iter = component.getScrollBar().iterator(); iter.hasNext();) {
            ScrollBar scrollBar = (ScrollBar) iter.next();
            if (scrollBar.getName().equals(type)) {

                ScrollBarSkinXML scrollBarXml = new ScrollBarSkinXML();
                scrollBarXml.setRollDownButton(scrollBar.getRollDownButton());
                scrollBarXml.setRollUpButton(scrollBar.getRollUpButton());
                scrollBarXml.setRollRigthButton(scrollBar.getRollRigthButton());
                scrollBarXml.setRollLeftButton(scrollBar.getRollLeftButton());

                return scrollBarXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Scroll Bar type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getStateByName(java.lang.String)
     */
    public SkinStateXML getStateByName(List states, String name) throws SkinException {

        for (Iterator iter = states.iterator(); iter.hasNext();) {
            State stateType = (State) iter.next();
            if (stateType.getName().equals(name)) {
                return toSkinStateXml(stateType);
            }
        }
        return null;
    }

    /**
     * @param stateType
     * @return
     */
    public SkinStateXML toSkinStateXml(State stateType) {
        SkinStateXML stateXml = new SkinStateXML();

        if (stateType.getBackgroundColor() != null) {
            // Definindo a cor de fundo
            ColorBean backgroundColor = new ColorBean(stateType.getBackgroundColor().getRed(), stateType.getBackgroundColor().getGreen(),
                                        stateType.getBackgroundColor().getBlue(), stateType.getBackgroundColor().getAlpha());
            stateXml.setBackGroundColor(backgroundColor);
        }
        // Definindo a fonte

        if (stateType.getFont() != null) {

            ColorBean fontColor = null;
            if (stateType.getFont().getColor() != null) {
                fontColor = new ColorBean(stateType.getFont().getColor().getRed(), stateType.getFont().getColor().getGreen(), stateType
                                          .getFont().getColor().getBlue(), 255);
            }
            
            FontBean font = new FontBean(stateType.getFont().getType(), stateType.getFont().getSize(), 
            		stateType.getFont().isBold(), stateType.getFont().isItalic(), stateType.getFont().isUnderlined(), fontColor);

            stateXml.setFont(font);
        }

        // Definndo nome e imagem do estado
        stateXml.setImagePath(stateType.getImagePath());
        stateXml.setName(stateType.getName());
        stateXml.setSpacement(stateType.getSpacement());

        return stateXml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getTable(java.lang.String)
     */
    public TableSkinXML getTable(String type) throws SkinException {
        for (Iterator iter = component.getTable().iterator(); iter.hasNext();) {
            Table tableType = (Table) iter.next();
            if (tableType.getName().equals(type)) {

                TableSkinXML tableSkinXml = new TableSkinXML();
                tableSkinXml.setDefaultState(getStateByName(tableType.getState(), DEFAULT_STATE));
                tableSkinXml.setTouched(getStateByName(tableType.getState(), TOUCHED_STATE));

                TableHeaderSkinXML tableHeaderSkinXml = getTableHeader(tableType.getName());
                tableSkinXml.setTableHeaderSkinXml(tableHeaderSkinXml);

                return tableSkinXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("table type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getTable(java.lang.String)
     */
    public TableHeaderSkinXML getTableHeader(String type) throws SkinException {
        for (Iterator iter = component.getTable().iterator(); iter.hasNext();) {
            Table tableType = (Table) iter.next();
            if (tableType.getName().equals(type)) {

                TableHeaderSkinXML tableHeaderSkinXml = new TableHeaderSkinXML();
                tableHeaderSkinXml.setDefaultState(toSkinStateXml(tableType.getTableHeader().getState()));

                return tableHeaderSkinXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("tableHeader type not found");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.reader.SkinReader#getDialog(java.lang.String)
     */
    public DialogSkinXML getDialog(String type) throws SkinException {

        for (Iterator iter = component.getDialog().iterator(); iter.hasNext();) {
            Dialog dialog = (Dialog) iter.next();
            if (dialog.getName().equals(type)) {

                DialogSkinXML dialogSkinXml = new DialogSkinXML();
                dialogSkinXml.setDefaultState(toSkinStateXml(dialog.getState()));

                return dialogSkinXml;

            }
        }
        // Caso nenhum button seja encontrado a exceção é lançada
        throw new SkinException("Dialog type not found");
    }
}