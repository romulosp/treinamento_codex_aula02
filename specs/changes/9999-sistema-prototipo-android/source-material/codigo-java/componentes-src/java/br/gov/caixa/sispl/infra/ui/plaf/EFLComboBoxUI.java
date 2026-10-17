package br.gov.caixa.sispl.infra.ui.plaf;

import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.plaf.metal.MetalComboBoxButton;
import javax.swing.plaf.metal.MetalComboBoxUI;

public class EFLComboBoxUI extends MetalComboBoxUI {

	protected JButton createArrowButton() {
		JButton button = new MetalComboBoxButton(comboBox,
				new EFLComboBoxIcon(), false,
				currentValuePane, listBox);
		button.setMargin(new Insets(0, 1, 1, 3));
		return button;
	}

}
