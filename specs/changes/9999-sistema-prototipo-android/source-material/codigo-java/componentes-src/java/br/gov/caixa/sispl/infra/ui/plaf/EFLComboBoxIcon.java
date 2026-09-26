package br.gov.caixa.sispl.infra.ui.plaf;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import javax.swing.plaf.metal.MetalComboBoxButton;
import javax.swing.plaf.metal.MetalComboBoxIcon;

import br.gov.caixa.sispl.infra.ui.EFLComboBox;

public class EFLComboBoxIcon extends MetalComboBoxIcon {

	private static final int EFL_COMBOBOX_IMAGE_BUTTON_WIDTH = 22;

	public void paintIcon(Component c, Graphics g, int x, int y) {
		MetalComboBoxButton comboBox = (MetalComboBoxButton) c;
		final EFLComboBox eflComboBox = (EFLComboBox) comboBox.getComboBox();
		g.drawImage(eflComboBox.getIcon().getImage(), comboBox.getWidth()
				- EFL_COMBOBOX_IMAGE_BUTTON_WIDTH, 0, new ImageObserver() {
			public boolean imageUpdate(Image img, int infoflags, int x, int y,
					int width, int height) {
				eflComboBox.repaint();
				return false;
			}
		});
	}
}
