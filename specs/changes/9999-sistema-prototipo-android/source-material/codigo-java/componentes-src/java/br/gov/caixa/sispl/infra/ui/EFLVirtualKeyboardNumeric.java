/*
 * Created on 16/03/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;

import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.util.Messages;

/**
 * @author p532406
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class EFLVirtualKeyboardNumeric extends JComponent {

    private UIManager uiManager;

    //Virtual KeyBoard Parameters
    private ArrayList teclas = null;

    /**
     * This is the default constructor
     */
    public EFLVirtualKeyboardNumeric(UIManager uiManager) {
        super();
        this.uiManager = uiManager;
        initialize();
    }

    /**
     * This method initializes this
     * 
     * @return void
     */
    private void initialize() {
        this.setOpaque(false);
        this.setLayout(null);
        this.setSize(200, 350);

        teclas = new ArrayList();
        teclas.add(new EFLVirtualKeyboardKey("0", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_0, new Point(64, 170), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("1", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_1, new Point(5, 115), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("2", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_2, new Point(64, 115), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("3", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_3, new Point(123, 115), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("4", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_4, new Point(5, 60), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("5", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_5, new Point(64, 60), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("6", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_6, new Point(123, 60), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("7", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_7, new Point(5, 5), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("8", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_8, new Point(64, 5), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("9", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_9, new Point(123, 5), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("<", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_LEFT, new Point(5, 170), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey(">", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_RIGHT, new Point(123, 170), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("TAB", ButtonSkin.BOTAO_TECLADO_AUXILIAR_1, KeyEvent.VK_TAB, new Point(5, 225), new Dimension(80, 60)));
        teclas.add(new EFLVirtualKeyboardKey(Messages.getString("EFLVirtualKeyboardNumeric.clear"), ButtonSkin.BOTAO_TECLADO_AUXILIAR_1, KeyEvent.VK_BACK_SPACE, new Point(98, 225), new Dimension(80, 60)));

        Iterator iteratorTeclas = teclas.iterator();
        while (iteratorTeclas.hasNext()) {
            EFLVirtualKeyboardKey eflVirtualKeyboardKey = (EFLVirtualKeyboardKey) iteratorTeclas.next();
            EFLButton teclaButton = new EFLButton(uiManager, eflVirtualKeyboardKey.getKeySkin());
            teclaButton.setText(eflVirtualKeyboardKey.getKeyLabel());
            teclaButton.setLocation(eflVirtualKeyboardKey.getLocation());
            teclaButton.setSize(eflVirtualKeyboardKey.getSize());
            teclaButton.addActionListener(new EFLVirtualKeyboardKeyActionListener(eflVirtualKeyboardKey));
            add(teclaButton);
        }
    }

    private class EFLVirtualKeyboardKeyActionListener extends SwingWorkerActionListener {

        private EFLVirtualKeyboardKey eflVirtualKeyBoardKey = null;

        public EFLVirtualKeyboardKeyActionListener(EFLVirtualKeyboardKey eflVirtualKeyboardKey) {
            this.eflVirtualKeyBoardKey = eflVirtualKeyboardKey;
        }

        public void actionPerformedInBackground(ActionEvent actionEvent) {
            uiManager.fireVirtualKeyboardKeyEvent(
                eflVirtualKeyBoardKey.getKeyCode(),
                EFLTextField.KEYMODIFIER_CAPSLOCK_OFF
            );
        }
    }

    private class EFLVirtualKeyboardKey {
        private String keyLabel;
        private String keySkin;
        private int keyCode;
        private Point location;
        private Dimension size;

        public EFLVirtualKeyboardKey(String keyLabel, String keySkin, int keyCode, Point location, Dimension size) {
            this.keyLabel = keyLabel;
            this.keySkin = keySkin;
            this.keyCode = keyCode;
            this.location = location;
            this.size = size;
        }

        /**
         * @return Returns the keyCode.
         */
        public int getKeyCode() {
            return keyCode;
        }

        /**
         * @param keyCode The keyCode to set.
         */
        public void setKeyCode(int keyCode) {
            this.keyCode = keyCode;
        }

        /**
         * @return Returns the keyLabel.
         */
        public String getKeyLabel() {
            return keyLabel;
        }

        /**
         * @param keyLabel The keyLabel to set.
         */
        public void setKeyLabel(String keyLabel) {
            this.keyLabel = keyLabel;
        }

        /**
         * @return Returns the keySkin.
         */
        public String getKeySkin() {
            return keySkin;
        }

        /**
         * @param keySkin The keySkin to set.
         */
        public void setKeySkin(String keySkin) {
            this.keySkin = keySkin;
        }

        /**
         * @return Returns the location.
         */
        public Point getLocation() {
            return location;
        }

        /**
         * @param location The location to set.
         */
        public void setLocation(Point location) {
            this.location = location;
        }

        /**
         * @return Returns the size.
         */
        public Dimension getSize() {
            return size;
        }

        /**
         * @param size The size to set.
         */
        public void setSize(Dimension size) {
            this.size = size;
        }
    }

}
