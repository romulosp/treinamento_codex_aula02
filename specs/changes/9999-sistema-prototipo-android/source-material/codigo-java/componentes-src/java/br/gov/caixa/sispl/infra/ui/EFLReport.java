/*
 * Created on 04/05/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;

import br.gov.caixa.sispl.infra.skin.component.label.LabelSkin;
import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;
import br.gov.caixa.sispl.infra.skin.component.scrollPane.ScrollBarSkin;

/**
 * @author p532313
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLReport extends EFLPanel {
    private static final int ESPACO_SOMBRA = 4;

    private static final int ALTURA_SEPARADOR = 5;
    private static final int ESPACO_RODAPE = 10;
    private static final int TAMANHO_CABECALHO = 26;
    private static final int TAMANHO_RODAPE = 32;
    private static final int TAMANHO_BARRA_ROLAGEM = 0;

    public static final int ENTRADA_SIMPLES = 0;
    public static final int ENTRADA_TITULO = 1;
    public static final int ENTRADA_TEXTO = 2;
    public static final int ENTRADA_FINAL = 3;
    public static final int ENTRADA_SELECIONADA = 4;
    public static final int ENTRADA_DUPLA = 5;
    public static final int ENTRADA_TEXTO_SEM_BORDA = 6;

    private static final int ALTURA_ENTRADA_SIMPLES = 20;
    private static final int ALTURA_ENTRADA_TITULO = 26;
    private static final int ALTURA_ENTRADA_TEXTO = 40;
    private static final int ALTURA_ENTRADA_DUPLA = 70;

    private EFLPanel panelReport = null;
    private EFLScrollPane scrollReport = null;
    private ArrayList paineisLinhas = null;

    private int entradasSimples = 0;
    private int entradasTitulos = 0;
    private int entradasTextos = 0;
    private int entradaFinal = 0;
    private int entradasDuplas = 0;

    private int separadores = 0;
    private int internalSize = 0;

    private int rolagemHorizontal = EFLScrollPane.HORIZONTAL_SCROLLBAR_NEVER;

    private EFLLabel decoradorCabecalho = null;
    private EFLLabel decoradorRodape = null;

    public EFLReport() {
        setOpaque(false);
        this.setLayout(null);
        paineisLinhas = new ArrayList();
        separadores = 0;
        initialize();
    }

    public EFLReport(int internalSize) {
        setOpaque(false);
        this.setLayout(null);
        paineisLinhas = new ArrayList();
        separadores = 0;
        rolagemHorizontal = EFLScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED;
        this.internalSize = internalSize;
        initialize();
    }


    public EFLPanel addPanel(int type) {
        EFLPanel novoPainel = null;
        internalSize = (internalSize>this.getWidth())?internalSize:this.getWidth();
        if (entradaFinal == 0) {
            switch (type) {
            case ENTRADA_SIMPLES :
                novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
                novoPainel.setPreferredSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_SIMPLES));
                novoPainel.setSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_SIMPLES));
                novoPainel.setLocation(0, getPosicaoAltura());
                entradasSimples++;
                break;

            case ENTRADA_SELECIONADA :
                novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SELECTED);
                novoPainel.setPreferredSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_SIMPLES));
                novoPainel.setSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_SIMPLES));
                novoPainel.setLocation(0, getPosicaoAltura());
                entradasSimples++;
                break;

            case ENTRADA_TITULO :
                novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_TITULO);
                novoPainel.setPreferredSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_TITULO));
                novoPainel.setSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_TITULO));
                novoPainel.setLocation(0, getPosicaoAltura());
                entradasTitulos++;
                break;

            case ENTRADA_TEXTO :
                novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_TEXTO);
                novoPainel.setPreferredSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM) , ALTURA_ENTRADA_TEXTO));
                novoPainel.setSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_TEXTO));
                novoPainel.setLocation(0, getPosicaoAltura());
                entradasTextos++;
                break;
            case ENTRADA_FINAL :
                novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_GRANDE);
                int alturaEntradaFinal = (this.getHeight() - (TAMANHO_CABECALHO + TAMANHO_RODAPE)) - (getPosicaoAltura() + (ESPACO_RODAPE * 2));
                novoPainel.setPreferredSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), alturaEntradaFinal));
                novoPainel.setSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), alturaEntradaFinal));
                novoPainel.setLocation(0, getPosicaoAltura());
                entradaFinal = alturaEntradaFinal;
                break;

            case ENTRADA_DUPLA :
                novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_TEXTO);
                novoPainel.setPreferredSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_DUPLA));
                novoPainel.setSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_DUPLA));
                novoPainel.setLocation(0, getPosicaoAltura());
                entradasDuplas++;
                break;

            case ENTRADA_TEXTO_SEM_BORDA :
                novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
                novoPainel.setPreferredSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM) , ALTURA_ENTRADA_TEXTO));
                novoPainel.setSize(new Dimension(internalSize - (ESPACO_SOMBRA + TAMANHO_BARRA_ROLAGEM), ALTURA_ENTRADA_TEXTO));
                novoPainel.setLocation(0, getPosicaoAltura());
                entradasTextos++;
                break;
            }

            novoPainel.setLayout(null);
            paineisLinhas.add(novoPainel);
            panelReport.setPreferredSize(new Dimension(internalSize - (ESPACO_SOMBRA + 3), getPosicaoAltura() + ESPACO_RODAPE));
            panelReport.setSize(new Dimension(internalSize - (ESPACO_SOMBRA + 3), getPosicaoAltura() + ESPACO_RODAPE));
            panelReport.add(novoPainel);
            novoPainel.repaint();
            panelReport.repaint();
        }
        return novoPainel;
    }

    public EFLPanel addPanel(EFLPanel novoPainel,int x , int y ) {
    	if(novoPainel != null) {
    		novoPainel.setLayout(null);
            paineisLinhas.add(novoPainel);
            panelReport.setPreferredSize(new Dimension(x - (ESPACO_SOMBRA + 3), y + ESPACO_RODAPE));
            panelReport.setSize(new Dimension(x - (ESPACO_SOMBRA + 3), y + ESPACO_RODAPE));
            panelReport.add(novoPainel);
            novoPainel.repaint();
            panelReport.repaint();
    	}
    	return novoPainel;
    }
    
    
    public EFLPanel addPanel(Collection jcomponents, int type) {
        EFLPanel novoPanel = addPanel(type);
        Iterator iterator = jcomponents.iterator();
        while (iterator.hasNext()) {
            novoPanel.add((JComponent) iterator.next());
        }
        return novoPanel;
    }

    public void addSeparator(int size) {
        separadores += size;
    }

    private int getPosicaoAltura() {
        int altura = (entradasSimples * ALTURA_ENTRADA_SIMPLES)
                     + (entradasTitulos * ALTURA_ENTRADA_TITULO)
                     + (entradasTextos * ALTURA_ENTRADA_TEXTO)
                     + (entradasDuplas * ALTURA_ENTRADA_DUPLA)
                     + (separadores * ALTURA_SEPARADOR)
                     + entradaFinal;
        return altura;
    }

    private void initialize() {
        this.add(getDecoradorCabecalho());
        this.add(getScrollReport());
        this.add(getDecoradorRodape());
    }

    public void setSize(int width, int height) {
        super.setSize(width, height);
        initialize();
    }

    private EFLLabel getDecoradorCabecalho() {
        if (decoradorCabecalho == null) {
            decoradorCabecalho = new EFLLabel(LabelSkin.LABEL_TABELA_CABECALHO_2);
            decoradorCabecalho.setLocation(0, 0);
        }
        decoradorCabecalho.setSize(this.getWidth(), TAMANHO_CABECALHO);
        return decoradorCabecalho;
    }

    private EFLLabel getDecoradorRodape() {
        if (decoradorRodape == null) {
            decoradorRodape = new EFLLabel(LabelSkin.LABEL_TABELA_RODAPE_2);
        }
        decoradorRodape.setSize(this.getWidth(), TAMANHO_RODAPE);
        decoradorRodape.setLocation(0, this.getHeight() - TAMANHO_RODAPE);
        return decoradorRodape;
    }

    private EFLPanel getPanelReport() {
        if (panelReport == null) {
            panelReport = new EFLPanel(PanelSkin.PANEL_TABELA);
            panelReport.setLocation(0, 0);
        }
        panelReport.setPreferredSize(new Dimension(this.getWidth() - (ESPACO_SOMBRA + 3), getPosicaoAltura() + ESPACO_RODAPE));
        panelReport.setSize(new Dimension(this.getWidth() - (ESPACO_SOMBRA + 3), getPosicaoAltura() + ESPACO_RODAPE));
        return panelReport;
    }

    private EFLScrollPane getScrollReport() {
        if (scrollReport == null) {
            scrollReport = new EFLScrollPane(ScrollBarSkin.TEST_BAR,EFLScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, rolagemHorizontal);
            scrollReport.setViewportView(getPanelReport());
            scrollReport.setLocation(0, TAMANHO_CABECALHO);
        }
        scrollReport.setSize(this.getWidth() - ESPACO_SOMBRA, this.getHeight() - (TAMANHO_CABECALHO + TAMANHO_RODAPE));
        return scrollReport;
    }
    
    public int getHorizontalScrollBarValue() {
    	return scrollReport.getHorizontalScrollBar().getValue();
    }

    public void setHorizontalScrollBarValue(int value) {
    	scrollReport.getHorizontalScrollBar().setValue(value);
    }

    public int getVerticalScrollBarValue() {
    	return scrollReport.getVerticalScrollBar().getValue();
    }

    public void setVerticalScrollBarValue(int value) {
    	scrollReport.getVerticalScrollBar().setValue(value);
    }

    public void setSizeReport(int width, int height){
    	panelReport.setPreferredSize(new Dimension(width, height));
        panelReport.setSize(new Dimension(width, height));
        panelReport.repaint();
    }
}

