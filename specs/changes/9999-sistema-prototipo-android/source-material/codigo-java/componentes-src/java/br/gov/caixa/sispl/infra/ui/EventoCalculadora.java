package br.gov.caixa.sispl.infra.ui;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import br.gov.caixa.sispl.infra.util.Calculadora;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;

/**
 * Classe responsável por gerenciar os Eventos de teclado gerados pelo teclado
 * exclusivamente numérico.
 */
public class EventoCalculadora {

    private static final Log logger = LogFactory.getLog(EventoCalculadora.class);

    /**
     * Representa o rawcode da tecla Multimedia MAIL
     */
    public static final Long VK_MM_NOTIFICATION = 163L;

    /**
     * Representa o rawcode da tecla Multimedia HOME
     */
    public static final Long VK_MM_HOME = 180L;

    /**
     * Representa o rawcode da tecla Multimedia CALC
     */
    public static final Long VK_MM_CALCULATOR = 148L;

    /**
     * Adiciona KeyEventDispatcher ao KeyboardFocusManager para tratar os eventos do teclado.
     */
    public static void addEventoCalculadora() {
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

        manager.addKeyEventDispatcher(new KeyEventDispatcher() {

            private Long getRawCode(KeyEvent e) {
                Field rawCodeField;
                Long rawCode = null;
                try {
                    rawCodeField = KeyEvent.class.getDeclaredField("rawCode");
                    rawCodeField.setAccessible(true);
                    rawCode = (Long) rawCodeField.get(e);
                } catch (Exception e1) {
                    logger.error("EFL : ERRO NA LEITURA DO RAWCODE DO EVENTO DO TECLADO", e1);
                }
                return rawCode;
            }

            /**
             * @see java.awt.KeyEventDispatcher#dispatchKeyEvent(java.awt.event.KeyEvent)
             */
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() == KeyEvent.KEY_RELEASED) {
                    if(e.getKeyCode() == KeyEvent.VK_UNDEFINED) {
                        Long rawCode = getRawCode(e);
                        if (EventoCalculadora.VK_MM_CALCULATOR.equals(rawCode)) {
                            toggleCalculadora();
                        }
                    } else if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_STANDARD
                            && e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                        toggleCalculadora();
                    }
                }

                return false;
            }

            private void toggleCalculadora() {
                Calculadora.getInstance().toggleVisible();
                EFLFrame.getInstance().requestFocus();
                EFLFrame.getInstance().getContentPane().requestFocusInWindow();
            }
        });
    }
}
