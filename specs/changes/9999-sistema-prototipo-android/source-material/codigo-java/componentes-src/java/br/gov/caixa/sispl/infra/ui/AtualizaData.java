package br.gov.caixa.sispl.infra.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import br.gov.caixa.sispl.util.Formatador;

/**
 * Classe responsável por atualizar o relógio do Frame da EFL com o relógio baseado no servidor.
 * @author p532313
 */
public class AtualizaData {
    private Timer timer;
    private AtualizaDataTimerTask atualizaDataTimerTask;

    /**
     * Método responsável por iniciar um processo que roda de 500 em 500 ms para atualizar o relógio da EFL.
     */
    public AtualizaData(EFLFrame eflFrame) {
        timer = new Timer();
        atualizaDataTimerTask = new AtualizaDataTimerTask(eflFrame);
        timer.schedule(atualizaDataTimerTask , 0, 500);
    }

    private static class AtualizaDataTimerTask extends TimerTask {

        private EFLFrame eflFrame;

        private SimpleDateFormat dateFormat;

        public AtualizaDataTimerTask(EFLFrame eflFrame) {
            dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            this.eflFrame = eflFrame;
        }

        public void run() {
            long horarioServidor = System.currentTimeMillis();
            String hora = Formatador.getInstance().formataHoraServidor(new Date(horarioServidor));
            String data = dateFormat.format(new Date(horarioServidor));
            eflFrame.setHoraBrasilia(hora);
            eflFrame.setDataBrasilia(data);
        }
    }
}