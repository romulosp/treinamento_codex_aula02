/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin.builder.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;

import br.gov.caixa.sispl.infra.skin.builder.SkinBuilder;
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
import br.gov.caixa.sispl.infra.skin.component.state.ColorBean;
import br.gov.caixa.sispl.infra.skin.component.state.FontBean;
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
import br.gov.caixa.sispl.infra.skin.exception.SkinException;
import br.gov.caixa.sispl.infra.skin.util.FontLoader;
import br.gov.caixa.sispl.infra.skin.util.ImageLoader;

/**
 * @author p532406 To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SkinBuilderImpl implements SkinBuilder {

    private static SkinBuilderImpl instance;

    public static SkinBuilderImpl getInstance() {
        if (instance == null)
            instance = new SkinBuilderImpl();
        return instance;
    }

    /**
     * @param skinStateXml
     * @return
     */
    public SkinState createState(SkinStateXML skinStateXml)
    throws SkinException {

        SkinState state = new SkinState();
        if (skinStateXml == null)
            return null;
        if (skinStateXml.getImagePath() != null)
            state.setImage(ImageLoader.getInstance().getImage(skinStateXml.getImagePath()));

        ColorBean color = skinStateXml.getBackGroundColor();
        if (color != null)
            state.setBackgroundColor(new Color(color.getRed(),
                                               color.getGreen(), color.getBlue(), color.getAlpha()));

        // String urlFont =
        // "C:/SISPL_RELEASE01_view/Loterias/SISPL/Implementacao/source/resources/efl/images";
        FontBean fontType = skinStateXml.getFont();

        state.setSpacement(skinStateXml.getSpacement());
        if (fontType != null) {

            try {
                //fontInputStream = this.getClass().getResourceAsStream(fontType.getType());

                int style = Font.PLAIN;
                if (fontType.isBold())
                    style += Font.BOLD;
                if (fontType.isItalic())
                    style += Font.ITALIC;

                Font font = FontLoader.getInstance().getFont(fontType.getType(), fontType.getSize());
                state.setFont(font);
                ColorBean fontColor = fontType.getColor();
                if (fontColor != null) {
                    state.setFontColor(new Color(fontColor.getRed(), fontColor
                                                 .getGreen(), fontColor.getBlue()));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new SkinException(e.getMessage());
            } catch (FontFormatException e) {
                e.printStackTrace();
                throw new SkinException(e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                throw new SkinException(e.getMessage());
            }

        }

        return state;

    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createButton(br.gov.caixa.sispl.infra.skin.component.button.ButtonSkinXML)
     */
    public ButtonSkin createButton(ButtonSkinXML component) {
        ButtonSkinXML buttonSkinXml = component;

        SkinState defaultState = createState(buttonSkinXml.getDefaultState());
        SkinState touchedState = createState(buttonSkinXml.getTouched());
        SkinState disabledState = createState(buttonSkinXml.getDisabled());

        ButtonSkin buttonSkin = new ButtonSkin();
        buttonSkin.setDefaultState(defaultState);
        buttonSkin.setDisabledState(disabledState);
        buttonSkin.setTouchedState(touchedState);

        return buttonSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createPanel(br.gov.caixa.sispl.infra.skin.component.panel.PanelSkinXML)
     */
    public PanelSkin createPanel(PanelSkinXML component) {
        PanelSkinXML painelSkinXml = component;

        SkinState defaultState = createState(painelSkinXml.getDefaultState());

        PanelSkin panelSkin = new PanelSkin();
        panelSkin.setDefaultState(defaultState);

        return panelSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createTextField(br.gov.caixa.sispl.infra.skin.component.textfield.TextFieldSkinXML)
     */
    public TextFieldSkin createTextField(TextFieldSkinXML component) {
        TextFieldSkinXML labelSkinXml = component;

        SkinState defaultState = createState(labelSkinXml.getDefaultState());
        SkinState focusedState = createState(labelSkinXml.getFocused());
        SkinState disabledState = createState(labelSkinXml.getDisabled());

        TextFieldSkin labelSkin = new TextFieldSkin();
        labelSkin.setDefaultState(defaultState);
        labelSkin.setDisabledState(disabledState);
        labelSkin.setFocusedState(focusedState);

        return labelSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createTextArea(br.gov.caixa.sispl.infra.skin.component.textarea.TextAreaSkinXML)
     */
    public TextAreaSkin createTextArea(TextAreaSkinXML component) {
        TextAreaSkinXML textAreaSkinXml = component;

        SkinState defaultState = createState(textAreaSkinXml.getDefaultState());
        SkinState disabledState = createState(textAreaSkinXml.getDisabled());
        SkinState focusedState = createState(textAreaSkinXml.getFocused());

        TextAreaSkin textAreaSkin = new TextAreaSkin();
        textAreaSkin.setDefaultState(defaultState);
        textAreaSkin.setDisabledState(disabledState);
        textAreaSkin.setFocusedState(focusedState);

        return textAreaSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createToggleButton(br.gov.caixa.sispl.infra.skin.component.togglebutton.ToggleButtonSkinXML)
     */
    public ToggleButtonSkin createToggleButton(ToggleButtonSkinXML component) {

        ToggleButtonSkinXML buttonSkinXml = component;
        SkinState defaultState = createState(buttonSkinXml.getDefaultState());
        SkinState touchedState = createState(buttonSkinXml.getTouched());
        SkinState disabledState = createState(buttonSkinXml.getDisabled());

        ToggleButtonSkin buttonSkin = new ToggleButtonSkin();
        buttonSkin.setDefaultState(defaultState);
        buttonSkin.setDisabledState(disabledState);
        buttonSkin.setTouchedState(touchedState);

        return buttonSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createLabel(br.gov.caixa.sispl.infra.skin.component.label.LabelSkinXML)
     */
    public LabelSkin createLabel(LabelSkinXML component) {

        LabelSkinXML labelSkinXml = component;

        SkinState defaultState = createState(labelSkinXml.getDefaultState());

        LabelSkin labelSkin = new LabelSkin();
        labelSkin.setDefaultState(defaultState);

        return labelSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createScrollPane(br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkinXML)
     */
    public ScrollBarSkin createScrollPane(ScrollBarSkinXML component) {
        ScrollBarSkinXML scrollSkinXml = component;

        ScrollBarSkin scrollSkin = new ScrollBarSkin();

        scrollSkin.setRollDownButton(scrollSkinXml.getRollDownButton());
        scrollSkin.setRollUpButton(scrollSkinXml.getRollUpButton());
        scrollSkin.setRollLeftButton(scrollSkinXml.getRollLeftButton());
        scrollSkin.setRollRigthButton(scrollSkinXml.getRollRigthButton());

        return scrollSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createPasswordField(br.gov.caixa.sispl.infra.skin.component.passwordfield.PasswordFieldSkinXML)
     */
    public PasswordFieldSkin createPasswordField(PasswordFieldSkinXML component) {

        PasswordFieldSkinXML labelSkinXml = component;

        SkinState defaultState = createState(labelSkinXml.getDefaultState());
        SkinState focusedState = createState(labelSkinXml.getFocused());
        SkinState disabledState = createState(labelSkinXml.getDisabled());

        PasswordFieldSkin labelSkin = new PasswordFieldSkin();
        labelSkin.setDefaultState(defaultState);
        labelSkin.setDisabledState(disabledState);
        labelSkin.setFocusedState(focusedState);

        return labelSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createTable(br.gov.caixa.sispl.infra.skin.component.table.TableSkinXML)
     */
    public TableSkin createTable(TableSkinXML component) {

        TableSkinXML tableSkinXml = component;

        SkinState defaultState = createState(tableSkinXml.getDefaultState());
        SkinState touchedState = createState(tableSkinXml.getTouched());
        SkinState disabledState = createState(tableSkinXml.getDisabled());

        TableSkin tableSkin = new TableSkin();
        tableSkin.setDefaultState(defaultState);
        tableSkin.setTouchedState(touchedState);
        tableSkin.setDisabledState(disabledState);

        // Criando column header da tabela
        tableSkin.setTableHeaderSkin(createTableHeader(tableSkinXml
                                     .getTableHeaderSkinXml()));

        return tableSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createTable(br.gov.caixa.sispl.infra.skin.component.table.TableSkinXML)
     */
    public TableHeaderSkin createTableHeader(TableHeaderSkinXML componentHeader) {
        TableHeaderSkinXML tableHeaderSkinXml = componentHeader;

        SkinState defaultHeaderState = createState(tableHeaderSkinXml
                                       .getDefaultState());

        TableHeaderSkin tableHeaderSkin = new TableHeaderSkin();
        tableHeaderSkin.setDefaultState(defaultHeaderState);

        return tableHeaderSkin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.gov.caixa.sispl.infra.skin.builder.SkinBuilder#createDialog(br.gov.caixa.sispl.infra.skin.component.dialog.DialogSkinXML)
     */
    public DialogSkin createDialog(DialogSkinXML component) {
        DialogSkinXML dialogSkinXml = component;

        SkinState defaultState = createState(dialogSkinXml.getDefaultState());

        DialogSkin dialogSkin = new DialogSkin();
        dialogSkin.setDefaultState(defaultState);

        return dialogSkin;
    }

}
