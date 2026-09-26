package br.gov.caixa.sispl.infra.ui.dlg;

import java.awt.Point;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JDialog;

import br.gov.caixa.sispl.infra.ui.EFLFrame;

public class EFLDialog extends JDialog {

    /**
     * This constructer takes in the alpha blending
     * Values for transparent windows.
     */
    public EFLDialog(String type, EFLFrame parent) throws Exception {
        super(parent);

        if (!System.getProperty("os.name").startsWith("Windows")) {
            setCursor(
                Toolkit.getDefaultToolkit().createCustomCursor(
                    new ImageIcon("").getImage(),
                    new Point(0, 0),
                    "transparente"
                )
            );
        }
    }
}
