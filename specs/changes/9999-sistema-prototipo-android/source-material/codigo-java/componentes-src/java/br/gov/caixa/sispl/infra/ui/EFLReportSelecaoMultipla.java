package br.gov.caixa.sispl.infra.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import br.gov.caixa.sispl.infra.skin.component.panel.PanelSkin;

/**
 * Extensão do EFLReport2 para permitir a seleção de múltiplas linhas de uma lista.
 * 
 * @author f744040
 *
 */
public class EFLReportSelecaoMultipla extends EFLReport2 {

	private ArrayList paineisSelecionados = new ArrayList();
	
	public EFLReportSelecaoMultipla(int internalSize) {
		super(internalSize);
	}
	
	/** 
	 * Este método foi sobrescrito para não desmarcar o(s) registro(s) selecionado(s) anteriormente, possibilitando a seleção múltipla.
	 * Também adiciona o registro na lista de selecionados caso o mesmo já não esteja nela, removendo-o caso contrário.
	 */
	@Override
	public void setSelected(int index) {
		Component[] componentsNovoSelecionado = ((EFLPanel) paineisLinhas.get(index)).getComponents();
		EFLPanel pnl = refreshPanel(index);
        for (int i = 0; i < componentsNovoSelecionado.length; i++) {
            Component component = componentsNovoSelecionado[i];
            pnl.add(component);
        }
        panelReport.remove((EFLPanel) paineisLinhas.set(index, pnl));
        
        if (paineisSelecionados.contains((Integer) index)) {
        	paineisSelecionados.remove((Integer) index);
        } else {
        	paineisSelecionados.add((Integer) index);
        }
	}
	
	/**
	 * Método sobrescrito para referenciar o listener local EFLReportActionListener.
	 */
	public EFLPanel addPanel() {
        EFLPanel novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
        novoPainel.setPreferredSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
        novoPainel.setSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
        novoPainel.setLocation(0, getPosicaoAltura());
        novoPainel.setLayout(null);
        novoPainel.addMouseListener(new EFLReportActionListener(this, paineisLinhas.size()));

        paineisLinhas.add(novoPainel);
        panelReport.setPreferredSize(new Dimension(internalSize, getPosicaoAltura() + ESPACO_RODAPE));
        panelReport.setSize(new Dimension(internalSize, getPosicaoAltura() + ESPACO_RODAPE));
        panelReport.add(novoPainel);
        novoPainel.repaint();
        panelReport.repaint();
        return novoPainel;
    }
	
	/**
	 * Método sobrescrito para referenciar o listener local EFLReportActionListener.
	 */
	private EFLPanel refreshPanel(int index) {
        EFLPanel novoPainel = null;
        if (!paineisSelecionados.contains((Integer) index) &&  isSelectable()) {
            novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SELECTED);
        } else {
            novoPainel = new EFLPanel(PanelSkin.PANEL_REPORT_SIMPLES);
        }
        novoPainel.setPreferredSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
        novoPainel.setSize(new Dimension(internalSize, ALTURA_ENTRADA_SIMPLES));
        novoPainel.setLocation(0, getPosicaoAltura(index));
        novoPainel.setLayout(null);
        novoPainel.addMouseListener(new EFLReportActionListener(this,index));

        panelReport.add(novoPainel);
        novoPainel.repaint();
        return novoPainel;
    }
	
	/**
	 * Método que retorna a lista com os indices das linhas selecionadas.
	 * @return
	 */
	public ArrayList getSelectedList() {
        return paineisSelecionados;
    } 
	
	private class EFLReportActionListener extends MouseAdapter {
        private int index;
        private EFLReportSelecaoMultipla eflReport;
        
        public EFLReportActionListener(EFLReportSelecaoMultipla eflReport, int index) {
            EFLReportActionListener.this.index=index;
            this.eflReport = eflReport;
        }

        public void mouseClicked(MouseEvent e) {
            eflReport.setSelected(EFLReportActionListener.this.index);
        }
    }
}
