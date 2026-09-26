package br.gov.caixa.sispl.infra.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import br.gov.caixa.sispl.infra.controle.monitoraefl.MonitoraEFL;
import br.gov.caixa.sispl.infra.util.log.Log;
import br.gov.caixa.sispl.infra.util.log.LogFactory;

/**
 * Classe do componente visual que indica o status de comunicação da EFL com o gateway
 * e os servidores HTTP (SISPL-C) e DNS.
 *
 * @author p543430 (Alexandre Gomes de Lima)
 * @author <a href="http://www.paulojeronimo.com">Paulo Jerônimo</a>
 */
public class MonitorPanel extends EFLPanel {

    //objeto responsável pelo log
    private static final Log log = LogFactory.getLog(MonitorPanel.class);

    /**
     * Objeto responsável pela monitoração dos serviços.
     */
    public MonitoraEFL monitoraEfl;

    private static final long serialVersionUID = 1L;

    /**
     * Labels com a descrição de cada servidor monitorado.
     */
    private MonitorLabel 	labelGateway,
    labelDns,
    labelHttp;

    /**
     * Cria uma instância desta classe.
     *
     */
    public MonitorPanel() {
        setSize(80, 50);

        // inicializando labels
        this.add(getLabelDns());
        this.add(getLabelGateway());
        this.add(getLabelHttp());

        //criando o objeto de monitoração
        monitoraEfl = new MonitoraEFL();

        //inicializando a monitoração
        monitoraEfl.iniciarMonitoracao();

        addMouseListener(
            new MouseListener() {
                long lastTime;

                public void mouseClicked(MouseEvent e) {
                    long currentTime = System.currentTimeMillis();
                    long nextTime = lastTime + 15 * 60 * 1000;
                    if (nextTime < currentTime) {
                        log.debug("Inciando monitoração da EFL...");
                        monitoraEfl.iniciarMonitoracao();
                        lastTime = currentTime;
                    } else if (log.isDebugEnabled())
                        log.debug("Ignorando Solicitacao de monitoracao. Será possível em [" + (nextTime - currentTime) + "] ms");
                }

            public void mouseEntered(MouseEvent e) {}
                public void mouseExited(MouseEvent e) {}
                public void mousePressed(MouseEvent e) {}
                public void mouseReleased(MouseEvent e) {}
            }
        );

    }

    // métodos que inicializam os labels
    private MonitorLabel getLabelGateway() {
        if (labelGateway == null) {
            labelGateway = new MonitorLabel("GATEWAY", MonitoraEFL.GATEWAY);
            labelGateway.setSize(150, 20);
            labelGateway.setLocation(0, 0);
        }
        return labelGateway;
    }

    private MonitorLabel getLabelDns() {
        if (labelDns == null) {
            labelDns = new MonitorLabel("DNS", MonitoraEFL.DNS);
            labelDns.setSize(150, 20);
            labelDns.setLocation(0, 15);
        }
        return labelDns;
    }

    private MonitorLabel getLabelHttp() {
        if (labelHttp == null) {
            labelHttp = new MonitorLabel("HTTP", MonitoraEFL.HTTP);
            labelHttp.setSize(150, 20);
            labelHttp.setLocation(0, 30);
        }
        return labelHttp;
    }
}