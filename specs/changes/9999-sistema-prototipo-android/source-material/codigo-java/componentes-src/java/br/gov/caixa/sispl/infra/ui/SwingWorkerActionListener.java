package br.gov.caixa.sispl.infra.ui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;


/*
 * Created on 28/06/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author p532313
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class SwingWorkerActionListener extends AbstractAction implements ActionListener {

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        final ActionEvent actionEvent = e;
        final SwingWorker swingWorker = new SwingWorker() {
                                            public Object construct() {
                                                return new ActionTask(actionEvent);
                                            }
                                        };
        swingWorker.start();
    }

    /**
     *Método que conterá o código que será executado no actionListener do cliente. 
     * @param actionEvent 
     */
    public abstract void actionPerformedInBackground(ActionEvent actionEvent);


    /**
     * 
     * @author p532313
     *
     * Classe responsável por executar o código do cliente em seu construtor.
     */
    private class ActionTask {
        /**
         * @param actionEvent
         */
        public ActionTask(ActionEvent actionEvent) {
            actionPerformedInBackground(actionEvent);
        }
    }
}
