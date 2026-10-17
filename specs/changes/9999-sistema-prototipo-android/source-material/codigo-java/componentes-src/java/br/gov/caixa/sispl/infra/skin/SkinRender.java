/*
 * Created on 24/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.skin;

import br.gov.caixa.sispl.infra.skin.builder.SkinBuilder;
import br.gov.caixa.sispl.infra.skin.builder.SkinBuilderFactory;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.reader.SkinReader;
import br.gov.caixa.sispl.infra.skin.reader.SkinReaderFactory;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class SkinRender {

    /**
     * 
     * @param name
     * @param type
     * @return
     */
    public static ComponentSkin renderComponent(String name, String type) {

        //Carrega as propriedades do arquivo xml
        SkinReader reader = SkinReaderFactory.getSkinReader();
        //Carrega um builder para o componente
        SkinBuilder skinBuilder = SkinBuilderFactory.getSkinBuilder();

        if (name.equals(ComponentSkin.BUTTON)) {
            return skinBuilder.createButton(reader.getButton(type));
        }
        if (name.equals(ComponentSkin.LABEL)) {
            return skinBuilder.createLabel(reader.getLabel(type));
        }
        if (name.equals(ComponentSkin.PANEL)) {
            return skinBuilder.createPanel(reader.getPanel(type));
        }
        if (name.equals(ComponentSkin.PASSWORD_FIELD)) {
            return skinBuilder.createPasswordField(reader.getPasswordField(type));
        }
        if (name.equals(ComponentSkin.SCROLL_BAR)) {
            return skinBuilder.createScrollPane(reader.getScrollPane(type));
        }
        if (name.equals(ComponentSkin.TEXT_AREA)) {
            return skinBuilder.createTextArea(reader.getTextArea(type));
        }
        if (name.equals(ComponentSkin.TEXT_FIELD)) {
            return skinBuilder.createTextField(reader.getTextField(type));
        }
        if (name.equals(ComponentSkin.TOGGLE_BUTTON)) {
            return skinBuilder.createToggleButton(reader.getToggleButton(type));
        }
        if (name.equals(ComponentSkin.TABLE)) {
            return skinBuilder.createTable(reader.getTable(type));
        }
        if (name.equals(ComponentSkin.DIALOG)) {
            return skinBuilder.createDialog(reader.getDialog((type)));
        }


        return null;
    }

}
