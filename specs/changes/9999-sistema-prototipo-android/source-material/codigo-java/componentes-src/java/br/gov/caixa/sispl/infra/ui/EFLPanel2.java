/**
 * Criado em : 25/11/2005
 * Ultima modificacao em : 01/12/2005
 */

package br.gov.caixa.sispl.infra.ui;



import java.awt.Graphics;

import javax.swing.JPanel;

import br.gov.caixa.sispl.infra.skin.SkinPainter;
import br.gov.caixa.sispl.infra.skin.SkinRender;
import br.gov.caixa.sispl.infra.skin.component.ComponentSkin;
import br.gov.caixa.sispl.infra.skin.component.SkinState;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.ui.UIAbstractControle;



/**
 *     EFLPainel que permite a modificacao do Skin, um artificil para poder
 * "selecionar" o painel
 * @author P543424 (Jadson Jose dos Santos)
 *
 */
public class EFLPanel2 extends JPanel implements SkinPainter {

    /* para compatibilidade com java 5*/
    private static final long serialVersionUID = 0;

    /* Skin do painel*/
    private PanelSkin panelSkinComponent;



    /**
     * Construtor Default
     *
     */
    public EFLPanel2() {
        super();
        this.setLayout(null);
    }



    /**
     * Construtor que recebe o Skin do painel
     * @param panelSkin (O Skin do painel)
     */
    public EFLPanel2(String panelSkin) {
        super();
        if (panelSkin != null)
            panelSkinComponent = (PanelSkin) SkinRender.renderComponent(ComponentSkin.PANEL, panelSkin);

        this.setLayout(null);
    }




    /**
     * Metodo que desenha o componente, deve ser chamando antes do metodo <code>paint()</code>
     * @param Graphics (Um objeto Graphics de Java)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paintComponent(Graphics g) {
        drawSkin(g);
        invalidate();
    }





    /**
     * Metodo que desenha o Skin do Painel
     * @param Graphics (Um objeto Graphics de Java)
     * @see br.caixa.sispl.skin.CaixaSkinRender#setSkin(java.lang.String)
     */
    public void drawSkin(Graphics g) {
        SkinState state = panelSkinComponent.getDefaultState();
        if (state.getImage() != null) {
            g.drawImage(panelSkinComponent.getDefaultState().getImage(), 0, 0, getWidth(), getHeight(), this);
        }
    }





    /**
     *    Nao sei o que esse metodo faz, só está aqui porque eu o copiei do
     * EFLPanel. Se alguem desconbrir que, por acaso, esse metodo nao serve para nada
     * por favor retire-o. 
     * 	  Se alguem souber para que serve por favor comente.
     * 
     * OBS.: Nao tinha JavaDoc antes
     * 
     * @param uiControle
     */
    public void setUIControle(UIAbstractControle uiControle) {   }





    /**
     * Metodo que desenha o painel
     * @param Graphics (Um objeto Graphics de Java)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    public void paint(Graphics g) {
        if (panelSkinComponent != null)
            super.paint(g);
        else {
            super.paintComponent(g);
            super.paintBorder(g);
            super.paintChildren(g);
        }

    }





    /**
     *   Deve ser um metodo comum aos objetos Graficos da Infra.
     *   Se alguem souber para que serve por favor comente.
     * 
     *   OBS.: NAO TINHA JavaDoc ANTES
     *   @return sempre retorna false
     */
    public boolean isMenu() {
        return false;
    }





    /**
     *     Metodo que seta o Skin do painel
     *     @param panelSkin (o Skin do painel)
     */
    public void setSkin(String panelSkin) {
        if (panelSkin != null)
            panelSkinComponent = (PanelSkin) SkinRender.renderComponent(ComponentSkin.PANEL, panelSkin);
        this.repaint();
    }





} //fim da classe

