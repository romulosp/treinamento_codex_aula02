package br.gov.caixa.sispl.infra.ui;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;

import br.gov.caixa.sispl.infra.ui.plaf.EFLComboBoxUI;

public class EFLComboBox extends JComboBox {

    private ImageIcon imageIcon = null;
    
    public EFLComboBox() {
    	setUI(new EFLComboBoxUI());
    	imageIcon = new ImageIcon(this.getClass().getResource("/seta_combo_box.png"));
    	setBackground(new Color(103,132,192));
    	setForeground(new Color(255,255,255));
    	setFocusable(false);
	}
	
    public ImageIcon getIcon() {
        return imageIcon;
    }

    public void setIcon(ImageIcon imageIcon) {
        this.imageIcon = imageIcon;
        repaint();
    }
    
	public void adicionaUf() {
		this.addItem("AC");
		this.addItem("AL");
		this.addItem("AM");
		this.addItem("AP");
		this.addItem("BA");
		this.addItem("CE");
		this.addItem("DF");
		this.addItem("ES");
		this.addItem("GO");
		this.addItem("MA");
		this.addItem("MG");
		this.addItem("MS");
		this.addItem("MT");
		this.addItem("PA");
		this.addItem("PB");
		this.addItem("PE");
		this.addItem("PI");
		this.addItem("PR");
		this.addItem("RJ");
		this.addItem("RN");
		this.addItem("RO");
		this.addItem("RR");
		this.addItem("RS");
		this.addItem("SC");
		this.addItem("SE");
		this.addItem("SP");
		this.addItem("TO");
	}
}
