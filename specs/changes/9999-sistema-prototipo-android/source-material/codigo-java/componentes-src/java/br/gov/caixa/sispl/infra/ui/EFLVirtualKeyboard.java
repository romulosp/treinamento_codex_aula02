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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;

import br.gov.caixa.sispl.infra.skin.component.button.ButtonSkin;
import br.gov.caixa.sispl.infra.skin.component.togglebutton.ToggleButtonSkin;
import br.gov.caixa.sispl.util.Messages;

/**
 * @author p532406 To change the template for this generated type comment go to Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and
 *         Comments
 */
public class EFLVirtualKeyboard extends JComponent {

	private static final long serialVersionUID = 2007120601L;

	private UIManager uiManager;

    // Virtual KeyBoard Parameters
    private ArrayList teclas = null;
    private EFLToggleButton teclaFixar;

    private EFLButton teclaCancelar = null;
    private EFLButton teclaConfirmar = null;

    private ActionListener confirmarListener;
    private ActionListener cancelarListener;
    
    private boolean mostrarTeclaLimpar;

    /**
     * This is the default constructor
     */
    public EFLVirtualKeyboard(UIManager uiManager) {
        super();
        this.uiManager = uiManager;
        mostrarTeclaLimpar = false;
        initialize();
    }

    public EFLVirtualKeyboard(UIManager uiManager, boolean mostrarTeclaLimpar) {
        super();
        this.uiManager = uiManager;
        this.mostrarTeclaLimpar = mostrarTeclaLimpar;
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
        this.setSize(800, 200);
        teclas = new ArrayList();
        teclas.add(new EFLVirtualKeyboardKey("Q", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_Q, new Point(120, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("W", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_W, new Point(180, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("E", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_E, new Point(240, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("R", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_R, new Point(300, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("T", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_T, new Point(360, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("Y", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_Y, new Point(420, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("U", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_U, new Point(480, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("I", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_I, new Point(540, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("O", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_O, new Point(600, 36), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("P", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_P, new Point(660, 36), new Dimension(52, 52)));

        teclas.add(new EFLVirtualKeyboardKey("A", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_A, new Point(140, 89), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("S", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_S, new Point(200, 89), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("D", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_D, new Point(260, 89), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("F", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_F, new Point(320, 89), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("G", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_G, new Point(380, 89), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("H", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_H, new Point(440, 89), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("J", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_J, new Point(500, 89), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("K", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_K, new Point(560, 89), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("L", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_L, new Point(620, 89), new Dimension(52, 52)));

        if(mostrarTeclaLimpar) {
            teclas.add(new EFLVirtualKeyboardKey(Messages.getString("EFLVirtualKeyboardNumeric.clear"), ButtonSkin.BOTAO_TECLADO_AUXILIAR_1, KeyEvent.VK_BACK_SPACE, new Point(680, 89), new Dimension(80, 60)));
        }

        teclas.add(new EFLVirtualKeyboardKey("Z", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_Z, new Point(140, 142), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("X", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_X, new Point(200, 142), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("C", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_C, new Point(260, 142), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("V", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_V, new Point(320, 142), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey(Messages.getString("EFLVirtualKeyboard.espace"), ButtonSkin.BOTAO_TECLADO_AUXILIAR_2, KeyEvent.VK_SPACE, new Point(375, 142),
                                             new Dimension(110, 60)));
        teclas.add(new EFLVirtualKeyboardKey("B", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_B, new Point(488, 142), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("N", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_N, new Point(548, 142), new Dimension(52, 52)));
        teclas.add(new EFLVirtualKeyboardKey("M", ButtonSkin.BOTAO_TECLADO, KeyEvent.VK_M, new Point(608, 142), new Dimension(52, 52)));
        // 89
        // 142

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

        add(getTeclaCancelar());
        add(getTeclaConfirmar());
        add(getTeclaFixar());

    }

    private EFLToggleButton getTeclaFixar() {
        if (teclaFixar == null) {
            teclaFixar = new EFLToggleButton(uiManager, ToggleButtonSkin.BOTAO_TECLADO_AUXILIAR_1, Messages.getString("EFLVirtualKeyboard.fix")); //$NON-NLS-1$
            teclaFixar.setLocation(33, 89);
            teclaFixar.setSize(80, 60);
        }
        return teclaFixar;
    }

    public EFLButton getTeclaCancelar() {
        if (teclaCancelar == null) {
            teclaCancelar = new EFLButton(uiManager, ButtonSkin.BOTAO_CANCELAR, Messages.getString("CANCELAR")); //$NON-NLS-1$
            teclaCancelar.setLocation(29, 142);
            teclaCancelar.setSize(110, 60);
            teclaCancelar.setCancelButton();
        }
        return teclaCancelar;
    }

    /*
     * Na tela de Logon, o texto "CANCELAR" do botão cancelar é substituido por "DESLIGAR".
     */
    public void mostraBotaoDesligar() {
        getTeclaCancelar().setText(Messages.getString("EFLVirtualKeyboard.turnof")); //$NON-NLS-1$
    }

    public EFLButton getTeclaConfirmar() {
        if (teclaConfirmar == null) {
            teclaConfirmar = new EFLButton(uiManager, ButtonSkin.BOTAO_CONFIRMAR, Messages.getString("CONFIRMAR")); //$NON-NLS-1$
            teclaConfirmar.setLocation(665, 140);
            teclaConfirmar.setSize(110, 60);
            teclaConfirmar.setDefaultButton();
            setEnabledConfirmar(true);
        }
        return teclaConfirmar;
    }

    /**
     * @param listener
     */
    public void setConfirmarButtonListener(ActionListener listener) {
        if (confirmarListener != null)
            getTeclaConfirmar().removeActionListener(confirmarListener);

        confirmarListener = listener;
        getTeclaConfirmar().addActionListener(listener);
    }

    /**
     * @param listener
     */
    public void setCancelarButtonListener(ActionListener listener) {
        if (cancelarListener != null)
            getTeclaCancelar().removeActionListener(cancelarListener);

        cancelarListener = listener;
        getTeclaCancelar().addActionListener(listener);

    }

    public void setEnabledConfirmar(boolean status) {
        getTeclaConfirmar().setEnabled(status);
    }

    public void setEnabledCancelar(boolean status) {
        getTeclaCancelar().setEnabled(status);
    }

    private class EFLVirtualKeyboardKeyActionListener extends SwingWorkerActionListener {

        private EFLVirtualKeyboardKey eflVirtualKeyBoardKey = null;

        public EFLVirtualKeyboardKeyActionListener(EFLVirtualKeyboardKey eflVirtualKeyboardKey) {
            this.eflVirtualKeyBoardKey = eflVirtualKeyboardKey;
        }

        public void actionPerformedInBackground(ActionEvent actionEvent) {
            uiManager.fireVirtualKeyboardKeyEvent(eflVirtualKeyBoardKey.getKeyCode(),
                                                  getTeclaFixar().isSelected() ? EFLTextField.KEYMODIFIER_CAPSLOCK_ON
                                                  : EFLTextField.KEYMODIFIER_CAPSLOCK_OFF);
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
