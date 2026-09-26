/*
 * Created on 12/04/2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package br.gov.caixa.sispl.infra.ui;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author João Paulo Q. dos Santos
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Data implements Runnable {
    /**
     * Contador usado apenas para identificar a thread que está executando o trabalho. 
     */
    private static int threadNumber = 1;

    private Thread timer;

    private String data;

    /**
     * Construtor da classe Relogio. Chama o método start da classe.
     * 
     * @roseuid 3DCAA1CC00FF
     */
    public Data() {
        start();
    }

    /**
     * Seta a cada 500 ms o label do horário do frame
     * 
     * @roseuid 3DCAA1CC0109
     */
    public void run() {

        //seta o horário
        while (true) {
            String padraoData = "dd/MM/yyyy";
            SimpleDateFormat sd = new SimpleDateFormat(padraoData);

            data = sd.format(new Date());

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
        }
    }

    /**
     * Cria um thread para o relógio e inicializa-o.
     * 
     * @roseuid 3DCAA1CC0113
     */
    public void start() {
        String threadName = "Data-" + (threadNumber++);
        if (timer == null) {
            timer = new Thread(this, threadName);
            timer.start();
        }
    }

    /**
     * @return
     */
    public String getDataBrasilia() {
        return data;
    }
}
